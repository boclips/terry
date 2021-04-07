package com.boclips.terry.infrastructure.outgoing.slack

import com.boclips.terry.Fake
import com.boclips.terry.infrastructure.outgoing.slack.PostFailure
import com.boclips.terry.infrastructure.outgoing.slack.PosterResponse
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster

class FakeSlackPoster : Fake, SlackPoster {
    lateinit var slackMessages: List<SlackMessage>
    lateinit var urlsUsed: List<String>
    var nextResponse: PosterResponse? = null

    init {
        reset()
    }

    override fun reset(): Fake = this
        .also {
            slackMessages = emptyList()
            urlsUsed = emptyList()
        }

    fun respondWith(response: PosterResponse): FakeSlackPoster = this
        .also { nextResponse = response }

    override fun chatPostMessage(slackMessage: SlackMessage, url: String): PosterResponse =
        if (url == "https://httpbin.org/status/401") {
            PostFailure(message = "401 UNAUTHORIZED")
        } else {
            nextResponse!!
        }
            .also { slackMessages = slackMessages + slackMessage }
            .also { urlsUsed = urlsUsed + url }
}
