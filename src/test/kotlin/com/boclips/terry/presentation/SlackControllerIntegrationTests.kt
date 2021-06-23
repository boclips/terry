package com.boclips.terry.presentation

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.terry.infrastructure.FakeClock
import com.boclips.terry.infrastructure.incoming.SlackSignature
import com.boclips.terry.infrastructure.outgoing.securecredentials.FakeSecureCredentialRetriever
import com.boclips.terry.infrastructure.outgoing.securecredentials.SecureCredential
import com.boclips.terry.infrastructure.outgoing.slack.FakeSlackPoster
import com.boclips.terry.infrastructure.outgoing.slack.PostSuccess
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo.SlackMessageVideoType.KALTURA
import com.boclips.terry.infrastructure.outgoing.videos.FakeVideoService
import com.boclips.terry.infrastructure.outgoing.videos.FoundKalturaVideo
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import testsupport.AbstractSpringIntegrationTest
import java.math.BigDecimal

// This is a rotated secret, used because we hard-code an example signed by Slack with this secret
@TestPropertySource(properties = ["slack.signingSecret=f873d35529ed55a0ab71ac068488684d"])
class SlackControllerIntegrationTests : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var slackPoster: FakeSlackPoster

    @Autowired
    lateinit var slackSignature: SlackSignature

    @Autowired
    lateinit var clock: FakeClock

    @Autowired
    lateinit var videoService: FakeVideoService

    @Autowired
    lateinit var credentialRetriever: FakeSecureCredentialRetriever

    @Autowired
    lateinit var kalturaClient: TestKalturaClient

    @BeforeEach
    fun setUp() {
        slackPoster.reset()
        videoService.reset()
        credentialRetriever.reset()
        clock.reset()
    }

    @Test
    fun `root path serves a terrific message`() {
        mockMvc.perform(
            get("/")
        )
            .andExpect(status().isOk)
            .andExpect(xpath("h1").string(containsString("Do as I say")))
    }

    @Test
    fun `can meet Slack's verification challenge`() {
        postFromSlack(
            """
            {
                "token": "sometoken",
                "challenge": "iamchallenging",
                "type": "url_verification"
            }
        """,
            path = "/slack"
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/json"))
            .andExpect(jsonPath("$.challenge", equalTo("iamchallenging")))
    }

    @Test
    fun `failing the request signature check results in 401`() {
        val timestamp = validTimestamp()
        postFromSlack(
            body = """
                    {
                        "token": "sometoken",
                        "challenge": "iamchallenging",
                        "type": "url_verification"
                    }
                """,
            timestamp = timestamp,
            signature = slackSignature.compute(timestamp.toString(), "different-body"),
            path = "/slack"
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `sending a timestamp older than 5 minutes results in 401`() {
        postFromSlack(
            body = """
            {
                "token": "sometoken",
                "challenge": "iamchallenging",
                "type": "url_verification"
            }
        """,
            timestamp = staleTimestamp(),
            path = "/slack"
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `it's a client error to send a malformed Slack verification request`() {
        postFromSlack(
            body = """
            {
                "token": "sometoken",
                "poo": "iamchallenging",
                "type": "url_verification"
            }""".trimIndent(),
            path = "/slack"
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `Slack mentions receive 200s and send responses`() {
        slackPoster.respondWith(PostSuccess(timestamp = BigDecimal(1231231)))

        postFromSlack(
            body = """
            {
                "token": "ZZZZZZWSxiZZZ2yIvs3peJ",
                "team_id": "T061EG9R6",
                "api_app_id": "A0MDYCDME",
                "event": {
                    "funny_unknown_property": "to-test-ignoring-unknown-properties",
                    "type": "app_mention",
                    "user": "U061F7AUR",
                    "text": "What ever *happened* to <@U0LAN0Z89>?",
                    "ts": "1515449438.000011",
                    "channel": "C0LAN2Q65",
                    "event_ts": "1515449438000011"
                },
                "type": "event_callback",
                "event_id": "Ev0MDYGDKJ",
                "event_time": 1515449438000011,
                "authed_users": [
                    "U0LAN0Z89"
                ]
            }""".trimIndent(),
            path = "/slack"
        )
            .andExpect(status().isOk)
            .andExpect(content().json("{}"))

        assertThat(slackPoster.slackMessages[0].text).startsWith("<@U061F7AUR> Some things you can do:")
        assertThat(slackPoster.slackMessages[0].channel).isEqualTo("C0LAN2Q65")
    }


    @Test
    fun `responds to safenote request with safenote URL`() {
        slackPoster.respondWith(PostSuccess(timestamp = BigDecimal(1231231)))

        credentialRetriever.respondWith(
            SecureCredential("https://penguins.com")
        )

        postFromSlack(
            body = """
            {
                "token": "ZZZZZZWSxiZZZ2yIvs3peJ",
                "team_id": "T061EG9R6",
                "api_app_id": "A0MDYCDME",
                "event": {
                    "funny_unknown_property": "to-test-ignoring-unknown-properties",
                    "type": "app_mention",
                    "user": "U061F7AUR",
                    "text": "Hey <@U0LAN0Z89>, can I have a new safenote for mythology-and-fiction-explained please bud?",
                    "ts": "1515449438.000011",
                    "channel": "C0LAN2Q65",
                    "event_ts": "1515449438000011"
                },
                "type": "event_callback",
                "event_id": "Ev0MDYGDKJ",
                "event_time": 1515449438000011,
                "authed_users": [
                    "U0LAN0Z89"
                ]
            }""".trimIndent(),
            path = "/slack"
        )
            .andExpect(status().isOk)
            .andExpect(content().json("{}"))

        assertThat(slackPoster.slackMessages)
            .isEqualTo(
                listOf(
                    SlackMessage(
                        text = """Sure <@U061F7AUR>, here are the credentials for "mythology-and-fiction-explained": https://penguins.com""",
                        channel = "C0LAN2Q65"
                    )
                )
            )
    }

    @Test
    fun `videos are retrieved`() {
        videoService.respondWith(
            FoundKalturaVideo(
                videoId = "resolvedId",
                title = "Boclips 4evah",
                description = "a description",
                thumbnailUrl = "blahblah",
                playbackId = "agreatplayback",
                streamUrl = null
            )
        )
        slackPoster.respondWith(PostSuccess(timestamp = BigDecimal(98765)))

        postFromSlack(
            body = """
            {
                "token": "ZZZZZZWSxiZZZ2yIvs3peJ",
                "team_id": "T061EG9R6",
                "api_app_id": "A0MDYCDME",
                "event": {
                    "type": "app_mention",
                    "user": "U061F7AUR",
                    "text": "<@U0LAN0Z89> can I get video asdfzxcv please?",
                    "ts": "1515449438.000011",
                    "channel": "C0LAN2Q65",
                    "event_ts": "1515449438000011"
                },
                "type": "event_callback",
                "event_id": "Ev0MDYGDKJ",
                "event_time": 1515449438000011,
                "authed_users": [
                    "U0LAN0Z89"
                ]
            }""".trimIndent(),
            path = "/slack"
        )
            .andExpect(status().isOk)

        assertThat(videoService.lastIdRequest).isEqualTo("asdfzxcv")
        assertThat(slackPoster.slackMessages).isEqualTo(
            listOf(
                SlackMessage(
                    channel = "C0LAN2Q65",
                    text = "<@U061F7AUR> Here are the video details for asdfzxcv:",
                    slackMessageVideos = listOf(
                        SlackMessageVideo(
                            imageUrl = "blahblah",
                            title = "Boclips 4evah",
                            videoId = "resolvedId",
                            type = KALTURA,
                            playbackId = "agreatplayback"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `transcript requests tag videos in Kaltura`() {
        val fixtureTimestamp: Long = 1554829933
        val body = transcriptRequestBody(
            entryId = "0_fgc6nmmt",
            channel = "CH1HFTDT2",
            userId = "UBS7V80PR",
            responseUrlEncoded = "https%3A%5C%2F%5C%2Fhooks.slack.com%5C%2Factions%5C%2FT04CX5TQC%5C%2F605457088357%5C%2FoJeQpwXhyqrppUBFrs7beIcp"
        )
        val fixtureSignature = "v0=bcbaee6fc659d43ce2e1151f136190de4c3284563981bf4ccf9a045c54d45f3e"

        clock.nextTime = fixtureTimestamp
        slackPoster.nextResponse = PostSuccess(timestamp = fixtureTimestamp.toBigDecimal())
        postFromSlack(
            body = body,
            contentType = MediaType.APPLICATION_FORM_URLENCODED,
            timestamp = fixtureTimestamp,
            signature = fixtureSignature,
            path = "/slack-interaction"
        )
            .andExpect(status().isOk)


        assertThat(
            slackSignature.compute(
                timestamp = fixtureTimestamp.toString(),
                body = body
            )
        ).isEqualTo(fixtureSignature)
        assertThat(kalturaClient.getBaseEntry("0_fgc6nmmt").tags).containsExactly("caption48british")
        assertThat(slackPoster.slackMessages).isEqualTo(
            listOf(
                SlackMessage(
                    channel = "CH1HFTDT2",
                    text = """<@UBS7V80PR> OK, I requested a transcript for "0_fgc6nmmt" (British English)."""
                )
            )
        )
        assertThat(slackPoster.urlsUsed).containsExactly(
            "https://hooks.slack.com/actions/T04CX5TQC/605457088357/oJeQpwXhyqrppUBFrs7beIcp"
        )
    }

    private fun transcriptRequestBody(
        entryId: String,
        channel: String,
        userId: String,
        responseUrlEncoded: String
    ): String =
        """payload=%7B%22type%22%3A%22block_actions%22%2C%22team%22%3A%7B%22id%22%3A%22T04CX5TQC%22%2C%22domain%22%3A%22boclips%22%7D%2C%22user%22%3A%7B%22id%22%3A%22$userId%22%2C%22username%22%3A%22andrew%22%2C%22name%22%3A%22andrew%22%2C%22team_id%22%3A%22T04CX5TQC%22%7D%2C%22api_app_id%22%3A%22AH343GQTZ%22%2C%22token%22%3A%22eboarPAkhN2k0rafp4SNNfGh%22%2C%22container%22%3A%7B%22type%22%3A%22message%22%2C%22message_ts%22%3A%221554829923.013200%22%2C%22channel_id%22%3A%22$channel%22%2C%22is_ephemeral%22%3Afalse%7D%2C%22trigger_id%22%3A%22596234579393.4439197828.ac1a9302100ccae9475d5c5a7a4ca5ab%22%2C%22channel%22%3A%7B%22id%22%3A%22$channel%22%2C%22name%22%3A%22terry-test-output%22%7D%2C%22message%22%3A%7B%22type%22%3A%22message%22%2C%22subtype%22%3A%22bot_message%22%2C%22text%22%3A%22This+content+can%27t+be+displayed.%22%2C%22ts%22%3A%221554829923.013200%22%2C%22username%22%3A%22Terry%22%2C%22bot_id%22%3A%22BH3ADPWM8%22%2C%22blocks%22%3A%5B%7B%22type%22%3A%22section%22%2C%22block_id%22%3A%22yC1E%22%2C%22text%22%3A%7B%22type%22%3A%22mrkdwn%22%2C%22text%22%3A%22%2A%3C%40$userId%3E+Here+are+the+video+details+for+1234%3A%2A%22%2C%22verbatim%22%3Afalse%7D%7D%2C%7B%22type%22%3A%22divider%22%2C%22block_id%22%3A%22KJi%22%7D%2C%7B%22type%22%3A%22section%22%2C%22block_id%22%3A%22ywI%22%2C%22text%22%3A%7B%22type%22%3A%22mrkdwn%22%2C%22text%22%3A%22Is+Islamic+State+Planning+Attacks+on+the+West%3F%22%2C%22verbatim%22%3Afalse%7D%2C%22accessory%22%3A%7B%22fallback%22%3A%22435x250px+image%22%2C%22image_url%22%3A%22https%3A%5C%2F%5C%2Fcdnapisec.kaltura.com%5C%2Fp%5C%2F1776261%5C%2Fthumbnail%5C%2Fentry_id%5C%2F$entryId%5C%2Fheight%5C%2F250%5C%2Fvid_slices%5C%2F3%5C%2Fvid_slice%5C%2F2%22%2C%22image_width%22%3A435%2C%22image_height%22%3A250%2C%22image_bytes%22%3A22949%2C%22type%22%3A%22image%22%2C%22alt_text%22%3A%22Is+Islamic+State+Planning+Attacks+on+the+West%3F%22%7D%7D%2C%7B%22type%22%3A%22section%22%2C%22block_id%22%3A%22ZjhdX%22%2C%22text%22%3A%7B%22type%22%3A%22mrkdwn%22%2C%22text%22%3A%22%2APlayback+ID%2A%5Cn$entryId%22%2C%22verbatim%22%3Afalse%7D%7D%2C%7B%22type%22%3A%22section%22%2C%22block_id%22%3A%22CSQ%22%2C%22text%22%3A%7B%22type%22%3A%22mrkdwn%22%2C%22text%22%3A%22%2APlayback+Provider%2A%5CnKaltura%22%2C%22verbatim%22%3Afalse%7D%7D%2C%7B%22type%22%3A%22section%22%2C%22block_id%22%3A%2264gj7%22%2C%22text%22%3A%7B%22type%22%3A%22mrkdwn%22%2C%22text%22%3A%22%2AVideo+ID%2A%5Cn5c54a6c7d8eafeecae072ca4%22%2C%22verbatim%22%3Afalse%7D%7D%2C%7B%22type%22%3A%22section%22%2C%22block_id%22%3A%22G7%5C%2FCK%22%2C%22text%22%3A%7B%22type%22%3A%22mrkdwn%22%2C%22text%22%3A%22Request+transcript%3A%22%2C%22verbatim%22%3Afalse%7D%2C%22accessory%22%3A%7B%22type%22%3A%22static_select%22%2C%22placeholder%22%3A%7B%22type%22%3A%22plain_text%22%2C%22text%22%3A%22Choose+transcript+type%22%2C%22emoji%22%3Atrue%7D%2C%22options%22%3A%5B%7B%22text%22%3A%7B%22type%22%3A%22plain_text%22%2C%22text%22%3A%22British+English%22%2C%22emoji%22%3Atrue%7D%2C%22value%22%3A%22%7B%5C%22code%5C%22%3A%5C%22british-english%5C%22%2C%5C%22entryId%5C%22%3A%5C%22$entryId%5C%22%7D%22%7D%2C%7B%22text%22%3A%7B%22type%22%3A%22plain_text%22%2C%22text%22%3A%22US+English%22%2C%22emoji%22%3Atrue%7D%2C%22value%22%3A%22%7B%5C%22code%5C%22%3A%5C%22us-english%5C%22%2C%5C%22entryId%5C%22%3A%5C%22$entryId%5C%22%7D%22%7D%5D%2C%22action_id%22%3A%2278%3D%22%7D%7D%5D%7D%2C%22response_url%22%3A%22$responseUrlEncoded%22%2C%22actions%22%3A%5B%7B%22type%22%3A%22static_select%22%2C%22action_id%22%3A%2278%3D%22%2C%22block_id%22%3A%22G7%5C%2FCK%22%2C%22selected_option%22%3A%7B%22text%22%3A%7B%22type%22%3A%22plain_text%22%2C%22text%22%3A%22British+English%22%2C%22emoji%22%3Atrue%7D%2C%22value%22%3A%22%7B%5C%22code%5C%22%3A%5C%22british-english%5C%22%2C%5C%22entryId%5C%22%3A%5C%22$entryId%5C%22%7D%22%7D%2C%22placeholder%22%3A%7B%22type%22%3A%22plain_text%22%2C%22text%22%3A%22Choose+transcript+type%22%2C%22emoji%22%3Atrue%7D%2C%22action_ts%22%3A%221554829933.145715%22%7D%5D%7D"""

    private val timestampBufferSeconds = 10

    private fun validTimestamp() =
        System.currentTimeMillis() / 1000 - (5 * 60) + timestampBufferSeconds

    private fun staleTimestamp() =
        validTimestamp() - timestampBufferSeconds - 1

    private fun postFromSlack(
        body: String,
        contentType: MediaType = MediaType.APPLICATION_JSON,
        timestamp: Long = validTimestamp(),
        signature: String = slackSignature.compute(timestamp = timestamp.toString(), body = body),
        path: String
    ): ResultActions =
        mockMvc.perform(
            post(path)
                .header("X-Slack-Request-Timestamp", timestamp)
                .header("X-Slack-Signature", signature)
                .contentType(contentType)
                .content(body)
        )
}
