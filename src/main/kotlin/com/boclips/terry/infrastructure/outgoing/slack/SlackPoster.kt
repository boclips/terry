package com.boclips.terry.infrastructure.outgoing.slack

interface SlackPoster {
    fun chatPostMessage(
        slackMessage: SlackMessage,
        url: String
    ): PosterResponse
}
