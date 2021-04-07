package com.boclips.terry.infrastructure.outgoing.slack

import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo.SlackMessageVideoType.KALTURA
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo.SlackMessageVideoType.YOUTUBE
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.DefaultComparator

class MessageConverterTest {
    @Test
    fun `can convert message to Slack format, adding interactive elements`() {
        MessageConverter().convert(
            SlackMessage(
                channel = "a channel",
                text = "some text",
                slackMessageVideos = listOf(
                    SlackMessageVideo(
                        imageUrl = "https://api.slack.com/img/blocks/bkb_template_images/palmtree.png",
                        title = "a lovely video",
                        videoId = "the-video-id123",
                        type = KALTURA,
                        playbackId = "1234"
                    )
                )
            )
        ).let { converted ->
            JSONAssert.assertEquals(
                "Bad message conversion:",
                """
            {
                "channel": "a channel",
                "blocks": [
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "*some text*"
                        }
                    },
                    {
                        "type": "divider"
                    },
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "a lovely video"
                        },
                        "accessory": {
                            "type": "image",
                            "image_url": "https://api.slack.com/img/blocks/bkb_template_images/palmtree.png",
                            "alt_text": "a lovely video"
                        }
                    },
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "*Playback ID*\n1234"
                        }
                    },
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "*Playback Provider*\nKaltura"
                        }
                    },
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "*Video ID*\nthe-video-id123"
                        }
                    },
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "Request transcript:"
                        },
                        "accessory": {
                            "type": "static_select",
                            "placeholder": {
                                "type": "plain_text",
                                "text": "Choose transcript type"
                            },
                            "options": [
                                {
                                    "text": {
                                        "type": "plain_text",
                                        "text": "Arabic to Arabic"
                                    },
                                    "value": "{\"code\":\"arabic\",\"entryId\":\"1234\"}"
                                },
                                {
                                    "text": {
                                        "type": "plain_text",
                                        "text": "British English"
                                    },
                                    "value": "{\"code\":\"british-english\",\"entryId\":\"1234\"}"
                                },
                                {
                                    "text": {
                                        "type": "plain_text",
                                        "text": "English to Arabic"
                                    },
                                    "value": "{\"code\":\"english-arabic-translation\",\"entryId\":\"1234\"}"
                                },
                                {
                                    "text": {
                                        "type": "plain_text",
                                        "text": "US English"
                                    },
                                    "value": "{\"code\":\"us-english\",\"entryId\":\"1234\"}"
                                }
                            ]
                        }
                    }
                ]
            }
            """,
                jacksonObjectMapper().writeValueAsString(converted),
                DefaultComparator(JSONCompareMode.STRICT)
            )
        }
    }

    @Test
    fun `non-Kaltura videos don't show interactive elements yet`() {
        MessageConverter().convert(
            SlackMessage(
                channel = "a channel",
                text = "some text",
                slackMessageVideos = listOf(
                    SlackMessageVideo(
                        imageUrl = "https://api.slack.com/img/blocks/bkb_template_images/palmtree.png",
                        title = "a lovely video",
                        videoId = "the-video-id123",
                        type = YOUTUBE,
                        playbackId = "1234"
                    )
                )
            )
        ).let { converted ->
            assertThat(converted.blocks.mapNotNull {
                when (it) {
                    is SlackViewSection ->
                        it.accessory?.type
                    SlackViewDivider ->
                        null
                }
            }).doesNotContain("static_select")
        }
    }
}
