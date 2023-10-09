package com.boclips.terry.application.decisions

import com.boclips.terry.application.ChatReply
import com.boclips.terry.application.Decision
import com.boclips.terry.application.VideoRetrieval
import com.boclips.terry.infrastructure.incoming.SlackEvent
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo
import com.boclips.terry.infrastructure.outgoing.videos.Error
import com.boclips.terry.infrastructure.outgoing.videos.FoundKalturaVideo
import com.boclips.terry.infrastructure.outgoing.videos.FoundVideo
import com.boclips.terry.infrastructure.outgoing.videos.FoundYouTubeVideo
import com.boclips.terry.infrastructure.outgoing.videos.MissingVideo
import org.springframework.stereotype.Component

@Component
class GimmeVideo : WhatToDo {
    override fun isMe(event: SlackEvent): Boolean {
        return extractVideoId(event) != null
    }

    override fun getDecision(event: SlackEvent): Decision {
        val videoId = extractVideoId(event)!!
        return Decision(
            log = "Retrieving video ID $videoId",
            action = VideoRetrieval(videoId) { videoServiceResponse ->
                when (videoServiceResponse) {
                    is FoundKalturaVideo ->
                        replyWithVideo(
                            foundVideo = videoServiceResponse,
                            type = SlackMessageVideo.SlackMessageVideoType.KALTURA,
                            event = event,
                            requestVideoId = videoId
                        )

                    is FoundYouTubeVideo ->
                        replyWithVideo(
                            foundVideo = videoServiceResponse,
                            type = SlackMessageVideo.SlackMessageVideoType.YOUTUBE,
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
    }

    private fun extractVideoId(event: SlackEvent): String? {
        return """.*video ([^ ]+).*""".toRegex().let { pattern ->
            pattern.matchEntire(event.text)?.groups?.get(1)?.value
        }
    }

    private fun replyWithVideo(
        foundVideo: FoundVideo,
        type: SlackMessageVideo.SlackMessageVideoType,
        event: SlackEvent,
        requestVideoId: String
    ): ChatReply {
        return ChatReply(
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
}
