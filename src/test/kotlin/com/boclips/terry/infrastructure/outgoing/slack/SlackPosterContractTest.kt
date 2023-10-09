package com.boclips.terry.infrastructure.outgoing.slack

import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo.SlackMessageVideoType.YOUTUBE
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class FakeSlackPosterTests : SlackPosterTests() {
    @BeforeEach
    fun setUp() {
        poster = FakeSlackPoster().respondWith(
            PostSuccess(timestamp = BigDecimal(System.currentTimeMillis() / 1000 + 1))
        )
    }
}

@Disabled
class HTTPSlackPosterTests : SlackPosterTests() {
    @BeforeEach
    fun setUp() {
        poster = HTTPSlackPoster(
            botToken = System.getenv("SLACK_BOT_TOKEN")
        )
    }
}

abstract class SlackPosterTests {
    var poster: SlackPoster? = null
    private val timeout = BigDecimal(30)

    @Test
    fun `successfully posts to a channel`() {
        val begin = BigDecimal(System.currentTimeMillis() / 1000)
        val response = poster!!.chatPostMessage(
            SlackMessage(
                text = "Hi there",
                channel = "#terry-test-output",
                slackMessageVideos = listOf(
                    SlackMessageVideo(
                        imageUrl = "https://www.boclips.com/hubfs/Boclips_November2018%20Theme/image/terry-610548e89d54257dccc9174c262f53e7.png",
                        title = "This is a really cool video",
                        videoId = "A Video Id",
                        type = YOUTUBE,
                        playbackId = "12345561359"
                    )
                )
            ),
            url = "https://slack.com/api/chat.postMessage"
        )
        when (response) {
            is PostSuccess ->
                assertThat(response.timestamp)
                    .isGreaterThanOrEqualTo(begin)
                    .isLessThan(begin + timeout)
            is PostFailure ->
                fail<String>("Post failed: $response")
        }
    }

    @Test
    fun `failures produce PostFailures`() {
        when (
            val response = poster!!.chatPostMessage(
                SlackMessage(
                    text = "I hope this won't work",
                    channel = "#terry-test-output"
                ),
                url = "https://httpbin.org/status/401"
            )
        ) {
            is PostSuccess ->
                fail<String>("Expected post to Slack to fail, but it was successful: $response")
            is PostFailure ->
                assertThat(response.message).contains("401")
        }
    }
}
