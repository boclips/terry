package com.boclips.terry.application

import com.boclips.terry.application.decisions.EventNotificationDecisionMaker
import com.boclips.terry.infrastructure.incoming.BlockActions
import com.boclips.terry.infrastructure.incoming.EventNotification
import com.boclips.terry.infrastructure.incoming.Malformed
import com.boclips.terry.infrastructure.incoming.SlackEvent
import com.boclips.terry.infrastructure.incoming.SlackRequest
import com.boclips.terry.infrastructure.incoming.VerificationRequest
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.slack.TranscriptCodeForEntryId
import com.boclips.terry.infrastructure.outgoing.transcripts.Failure
import com.boclips.terry.infrastructure.outgoing.transcripts.Success
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component

data class TranscriptCode(
    val tag: String,
    val displayName: String
)

@Component
class Terry(val eventNotificationDecisionMaker: EventNotificationDecisionMaker) {
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
            is VerificationRequest -> Decision(VerificationResponse(request.challenge), "Responding to verification challenge")
            is EventNotification -> handleEventNotification(request.event)
            is BlockActions -> handleTranscriptRequest(blockActionsToTranscriptRequest(request))
            is Malformed -> Decision(MalformedRequestRejection, "Malformed request")
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
                }
            )
        } ?: MalformedRequestRejection

    private fun blockActionsToTranscriptRequest(blockActions: BlockActions): TranscriptRequest =
        (
            jacksonObjectMapper().readValue(
                blockActions.actions.first().selectedOption.value,
                TranscriptCodeForEntryId::class.java
            )
            )
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
        return eventNotificationDecisionMaker.tellMeWhatToDo(event)
    }
}

data class TranscriptRequest(
    val entryId: String,
    val channel: String,
    val user: String,
    val code: String,
    val responseUrl: String
)
