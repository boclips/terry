package com.boclips.terry.application

import com.boclips.terry.infrastructure.incoming.AppMention
import com.boclips.terry.infrastructure.incoming.BlockActions
import com.boclips.terry.infrastructure.incoming.EventNotification
import com.boclips.terry.infrastructure.incoming.Malformed
import com.boclips.terry.infrastructure.incoming.SlackEvent
import com.boclips.terry.infrastructure.incoming.SlackRequest
import com.boclips.terry.infrastructure.incoming.VerificationRequest
import com.boclips.terry.infrastructure.outgoing.securecredentials.CredentialNotFound
import com.boclips.terry.infrastructure.outgoing.securecredentials.SafenoteFailure
import com.boclips.terry.infrastructure.outgoing.securecredentials.SecureCredential
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo.SlackMessageVideoType.KALTURA
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo.SlackMessageVideoType.YOUTUBE
import com.boclips.terry.infrastructure.outgoing.slack.TranscriptCodeForEntryId
import com.boclips.terry.infrastructure.outgoing.transcripts.Failure
import com.boclips.terry.infrastructure.outgoing.transcripts.Success
import com.boclips.terry.infrastructure.outgoing.videos.Error
import com.boclips.terry.infrastructure.outgoing.videos.FoundKalturaVideo
import com.boclips.terry.infrastructure.outgoing.videos.FoundVideo
import com.boclips.terry.infrastructure.outgoing.videos.FoundYouTubeVideo
import com.boclips.terry.infrastructure.outgoing.videos.MissingVideo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component

data class TranscriptCode(
    val tag: String,
    val displayName: String
)

@Component
class Terry {
    companion object {
        val transcriptCodeToKalturaTag = mapOf(
            "british-english" to TranscriptCode(
                tag = "caption48british",
                displayName = "British English"
            ),
            "us-english" to TranscriptCode(
                tag = "caption48",
                displayName = "US English"
            ),
            "arabic" to TranscriptCode(
                tag = "arabic48",
                displayName = "Arabic to Arabic"
            ),
            "english-arabic-translation" to TranscriptCode(
                tag = "englisharabic48",
                displayName = "English to Arabic"
            )
        )
    }

    fun receiveSlack(request: SlackRequest): Decision =
        when (request) {
            is VerificationRequest ->
                Decision(
                    log = "Responding to verification challenge",
                    action = VerificationResponse(challenge = request.challenge)
                )

            is EventNotification ->
                handleEventNotification(request.event)

            is BlockActions ->
                handleTranscriptRequest(blockActionsToTranscriptRequest(request))

            Malformed ->
                Decision(
                    log = "Malformed request",
                    action = MalformedRequestRejection
                )
        }

    private fun handleTranscriptRequest(request: TranscriptRequest): Decision =
        Decision(
            log = "Transcript requested for ${request.entryId}",
            action = transcriptResponse(request)
        )

    private fun transcriptResponse(request: TranscriptRequest): Action =
        transcriptCodeToKalturaTag[request.code]?.let { transcriptCode ->
            VideoTagging(
                entryId = request.entryId,
                tag = transcriptCode.tag,
                responseUrl = request.responseUrl,
                onComplete =
                { response ->
                    when (response) {
                        is Success ->
                            ChatReply(
                                slackMessage = SlackMessage(
                                    channel = request.channel,
                                    text = """<@${request.user}> OK, I requested a transcript for "${response.entryId}" (${transcriptCode.displayName})."""
                                )
                            )

                        is Failure ->
                            ChatReply(
                                slackMessage = SlackMessage(
                                    channel = request.channel,
                                    text = """<@${request.user}> Sorry! I don't think "${response.entryId}" could be tagged: "${response.error}"."""
                                )
                            )
                    }
                })
        } ?: MalformedRequestRejection

    private fun blockActionsToTranscriptRequest(blockActions: BlockActions): TranscriptRequest =
        (jacksonObjectMapper().readValue(
            blockActions.actions.first().selectedOption.value,
            TranscriptCodeForEntryId::class.java
        ))
            .let { videoCode ->
                TranscriptRequest(
                    entryId = videoCode.entryId,
                    code = videoCode.code,
                    channel = blockActions.channel.id,
                    user = blockActions.user.id,
                    responseUrl = blockActions.responseUrl
                )
            }

    private fun handleEventNotification(event: SlackEvent): Decision {
        return when (event) {
            is AppMention -> {
                extractVideoId(event.text)?.let { videoId ->
                    videoRetrieval(videoId, event)
                } ?: extractSafenoteChannelName(event.text)?.let { channelName ->
                    channelUploadCredentialRetrieval(channelName, event)
                } ?: extractChannelCreationChannelName(event.text)?.let { channelName ->
                    channelCreation(channelName, event)
                } ?: extractSentryReportParams(event.text)?.let { reportParams ->
                    createSentryReport(reportParams, event)
                } ?: help(event)
            }
        }
    }

    private fun createSentryReport(params: SentryReportParams, event: AppMention): Decision {
        return Decision(
            log = "Generating sentry report",
            action = SentryReportCreation(params) { response ->
                ChatReply(
                    slackMessage = SlackMessage(
                        channel = event.channel,
                        text = "Sure <@${event.user}>, sizzling sentry report for you!\n${response.report}"
                    )
                )
            }
        )
    }

    private fun extractSentryReportParams(text: String): SentryReportParams? {
        return SentryReportParams().takeIf { text.lowercase().contains("sentry report") }
    }

    private fun extractChannelCreationChannelName(text: String): String? =
        extractChannelName(text, """.*(?:bucket|channel)(?: for)? ([a-z0-9-_ ]+).*""")

    private fun extractSafenoteChannelName(text: String): String? =
        extractChannelName(text, """.*safenote(?: for)? ([a-z0-9-_ ]+).*""")

    private fun extractChannelName(text: String, pattern: String): String? =
        pattern.toRegex(RegexOption.IGNORE_CASE).matchEntire(text)
            ?.groups
            ?.get(1)
            ?.value
            ?.toLowerCase()
            ?.replace(" please.*$".toRegex(), "")
            ?.replace(" ", "-")

    private fun help(event: AppMention) = Decision(
        log = "Responding via chat with \"${helpFor(event.user)}\"",
        action = ChatReply(
            slackMessage = SlackMessage(
                channel = event.channel,
                text = helpFor(event.user)
            )
        )
    )

    private fun channelCreation(channelName: String, event: AppMention) = Decision(
        log = "Creating channel $channelName",
        action = ChannelCreation(channelName) { response ->

            val text = when (response) {
                is ChannelCreationSuccess -> """<@${event.user}> I've created "$channelName"! You can use "@terrybot safenote $channelName" to generate a safenote."""
                is ChannelAlreadyExists -> """<@${event.user}> A bucket for that channel already exists. You can use "@terrybot safenote $channelName" to generate a safenote if you'd like, or not."""
                is InvalidChannelName -> """<@${event.user}> "$channelName" is not a valid bucket name 🪣 😤 🤯"""
                is ChannelCreationFailed -> """<@${event.user}> could not create a bucket at this time for an unknown reason 🤔"""
            }

            ChatReply(
                slackMessage = SlackMessage(
                    text = text,
                    channel = event.channel,
                )
            )
        }
    )

    private fun channelUploadCredentialRetrieval(channelName: String, event: AppMention) = Decision(
        log = "Retrieving safenote for $channelName",
        action = ChannelUploadCredentialRetrieval(channelName) { response ->
            when (response) {
                is SecureCredential -> ChatReply(
                    slackMessage = SlackMessage(
                        text = """Sure <@${event.user}>, here are the credentials for "$channelName": ${response.url}""",
                        channel = event.channel
                    )
                )

                CredentialNotFound -> ChatReply(
                    slackMessage = SlackMessage(
                        text = """Sorry <@${event.user}>, I can't find "$channelName" - maybe check the name?""",
                        channel = event.channel
                    )
                )

                is SafenoteFailure -> ChatReply(
                    slackMessage = SlackMessage(
                        text = "Sorry <@${event.user}>, the Safenote service isn't working! Ask an engineer? (${response.message})",
                        channel = event.channel
                    )
                )
            }
        }
    )

    private fun videoRetrieval(
        videoId: String,
        event: AppMention
    ) = Decision(
        log = "Retrieving video ID $videoId",
        action = VideoRetrieval(videoId) { videoServiceResponse ->
            when (videoServiceResponse) {
                is FoundKalturaVideo ->
                    replyWithVideo(
                        foundVideo = videoServiceResponse,
                        type = KALTURA,
                        event = event,
                        requestVideoId = videoId
                    )

                is FoundYouTubeVideo ->
                    replyWithVideo(
                        foundVideo = videoServiceResponse,
                        type = YOUTUBE,
                        event = event,
                        requestVideoId = videoId
                    )

                is MissingVideo ->
                    ChatReply(
                        slackMessage = SlackMessage(
                            channel = event.channel,
                            text = """<@${event.user}> Sorry, video $videoId doesn't seem to exist! :("""
                        )
                    )

                is Error ->
                    ChatReply(
                        slackMessage = SlackMessage(
                            channel = event.channel,
                            text = """<@${event.user}> looks like the video service is broken :( \n ${videoServiceResponse.message}"""
                        )
                    )
            }
        }
    )

    private fun extractVideoId(text: String): String? =
        """.*video ([^ ]+).*""".toRegex().let { pattern ->
            pattern.matchEntire(text)?.groups?.get(1)?.value
        }

    private fun helpFor(user: String): String = "<@$user> ${help()}"
    private fun help(): String = """
        Some things you can do:
        video 1234 (retrieves a video and displays a menu)
        safenote a-channel-name (retrieves a new Safenote for an existing channel's upload credentials)
        bucket for a-channel-name (creates a bucket in AWS for a-channel-name)
    """.trimIndent()

    private fun replyWithVideo(
        foundVideo: FoundVideo,
        type: SlackMessageVideo.SlackMessageVideoType,
        event: AppMention,
        requestVideoId: String
    ) =
        ChatReply(
            slackMessage = SlackMessage(
                channel = event.channel,
                text = "<@${event.user}> Here are the video details for $requestVideoId:",
                slackMessageVideos = listOf(
                    SlackMessageVideo(
                        imageUrl = foundVideo.thumbnailUrl,
                        title = foundVideo.title,
                        videoId = foundVideo.videoId,
                        type = type,
                        playbackId = foundVideo.playbackId
                    )
                )
            )
        )
}

data class TranscriptRequest(
    val entryId: String,
    val channel: String,
    val user: String,
    val code: String,
    val responseUrl: String
)
