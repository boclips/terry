package com.boclips.terry.infrastructure.outgoing.slack

import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo.SlackMessageVideoType.KALTURA
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class TranscriptCodeForEntryId(
    val code: String,
    val entryId: String
)

class MessageConverter {
    fun convert(slackMessage: SlackMessage): SlackView =
        messageToView(slackMessage)

    private fun messageToView(slackMessage: SlackMessage): SlackView =
        slackMessage.slackMessageVideos
            .fold(emptyList()) { acc: List<SlackViewBlock>, slackMessageVideo: SlackMessageVideo ->
                acc + standardSections(slackMessageVideo) + interactiveSections(slackMessageVideo)
            }
            .let { videoBlocks ->
                SlackView(
                    channel = slackMessage.channel,
                    blocks = listOf(
                        textSection("${slackMessage.text}")
                    ) + videoBlocks
                )
            }

    private fun standardSections(slackMessageVideo: SlackMessageVideo): List<SlackViewBlock> {
        return listOf(
            SlackViewDivider,
            SlackViewSection(
                type = "section",
                text = SlackViewText(
                    type = "mrkdwn",
                    text = slackMessageVideo.title
                ),
                accessory = SlackViewAccessory(
                    type = "image",
                    imageUrl = slackMessageVideo.imageUrl,
                    altText = slackMessageVideo.title
                )
            ),
            textSection("*Playback ID*\n${slackMessageVideo.playbackId}"),
            textSection("*Playback Provider*\n${slackMessageVideo.type.provider}"),
            textSection("*Video ID*\n${slackMessageVideo.videoId}")
        )
    }

    private fun interactiveSections(slackMessageVideo: SlackMessageVideo): List<SlackViewBlock> =
        if (slackMessageVideo.type == KALTURA) {
            listOf(
                SlackViewSection(
                    type = "section",
                    text = SlackViewText(
                        type = "mrkdwn",
                        text = "Request transcript:"
                    ),
                    accessory = SlackViewAccessory(
                        type = "static_select",
                        placeholder = SlackViewText(
                            type = "plain_text",
                            text = "Choose transcript type"
                        ),
                        options = Terry.transcriptCodeToKalturaTag.entries
                            .map { entry ->
                                SlackViewSelectOption(
                                    text = SlackViewText(
                                        type = "plain_text",
                                        text = entry.value.displayName
                                    ),
                                    value = createTranscriptValueJson(
                                        code = entry.key,
                                        slackMessageVideo = slackMessageVideo
                                    )
                                )
                            }
                            .sortedBy { entry -> entry.text.text }
                    )
                )
            )
        } else {
            emptyList()
        }

    private fun textSection(text: String): SlackViewSection {
        return SlackViewSection(
            type = "section",
            text = SlackViewText(
                type = "mrkdwn",
                text = text
            ),
            accessory = null
        )
    }

    private fun createTranscriptValueJson(code: String, slackMessageVideo: SlackMessageVideo): String {
        val transcriptRequest = TranscriptCodeForEntryId(
            code = code,
            entryId = slackMessageVideo.playbackId!!
        )

        val mapper: ObjectMapper = jacksonObjectMapper()
        return mapper.writeValueAsString(transcriptRequest)
    }
}
