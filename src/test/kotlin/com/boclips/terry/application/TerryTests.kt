package com.boclips.terry.application

import com.boclips.terry.infrastructure.incoming.AppMention
import com.boclips.terry.infrastructure.incoming.BlockAction
import com.boclips.terry.infrastructure.incoming.BlockActionIdentifiable
import com.boclips.terry.infrastructure.incoming.BlockActionSelectedOption
import com.boclips.terry.infrastructure.incoming.BlockActions
import com.boclips.terry.infrastructure.incoming.EventNotification
import com.boclips.terry.infrastructure.incoming.VerificationRequest
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo.SlackMessageVideoType.KALTURA
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo.SlackMessageVideoType.YOUTUBE
import com.boclips.terry.infrastructure.outgoing.transcripts.Failure
import com.boclips.terry.infrastructure.outgoing.transcripts.Success
import com.boclips.terry.infrastructure.outgoing.videos.Error
import com.boclips.terry.infrastructure.outgoing.videos.FoundKalturaVideo
import com.boclips.terry.infrastructure.outgoing.videos.FoundYouTubeVideo
import com.boclips.terry.infrastructure.outgoing.videos.MissingVideo
import io.kotlintest.properties.assertAll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.util.Date

class TerryTests {
    private val irrelevant: String = "irrelevant"

    @Test
    fun `verifies Slack`() {
        assertThat(
            Terry().receiveSlack(
                request = VerificationRequest(
                    challenge = "bet-you-cant-copy-paste-this-m8",
                    type = irrelevant
                )
            )
        ).isEqualTo(
            Decision(
                action = VerificationResponse(
                    challenge = "bet-you-cant-copy-paste-this-m8"
                ),
                log = "Responding to verification challenge"
            )
        )
    }

    @Test
    fun `responds to Slack enquiry about his job description`() {
        assertThat(mentionTerry("hi Tezza", user = "UBS7V80PR", channel = "#engineering"))
            .isEqualTo(
                Decision(
                    action = ChatReply(
                        slackMessage = SlackMessage(
                            channel = "#engineering",
                            text = "<@UBS7V80PR> I don't do much yet"
                        )
                    ),
                    log = """Responding via chat with "<@UBS7V80PR> I don't do much yet""""
                )
            )
    }

    @Test
    fun `retrieves video details when given an ID`() {
        assertAll { videoId: Long ->
            val decision = mentionTerry("I would like video $videoId", channel = "#engineering")
            assertThat(decision.log).isEqualTo("Retrieving video ID $videoId")
            when (val response = decision.action) {
                is VideoRetrieval -> {
                    assertThat(response.videoId).isEqualTo("$videoId")
                }
                else ->
                    fail<String>("Expected a video retrieval, but got $response")
            }
        }
    }

    @Test
    fun `successful receipt of Kaltura video triggers a chat message with the Kaltura details`() {
        when (val action = mentionTerry(
            "Yo can I get video myvid123 please?",
            user = "THAD123",
            channel = "#engineering"
        ).action) {
            is VideoRetrieval ->
                assertThat(
                    action.onComplete(
                        FoundKalturaVideo(
                            videoId = "abcdefg",
                            title = "Boclips 4evah",
                            description = "boclips is...interesting",
                            thumbnailUrl = "validurl",
                            playbackId = "1234",
                            streamUrl = "https://cdnapisec.kaltura.com/p/1776261/sp/177626100/playManifest/entryId/1_y0g6ftvy/format/applehttp/protocol/https/video.mp4"
                        )
                    )
                )
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = "<@THAD123> Here are the video details for myvid123:",
                                slackMessageVideos = listOf(
                                    SlackMessageVideo(
                                        imageUrl = "validurl",
                                        title = "Boclips 4evah",
                                        videoId = "abcdefg",
                                        type = KALTURA,
                                        playbackId = "1234"
                                    )
                                )
                            )
                        )
                    )
            else ->
                fail<String>("Expected a video retrieval, but got $action")
        }
    }

    @Test
    fun `successful receipt of YouTube video triggers a chat message with the YouTube details`() {
        when (val action = mentionTerry(
            "Yo can I get video myvid123 please?",
            user = "THAD123",
            channel = "#engineering"
        ).action) {
            is VideoRetrieval ->
                assertThat(
                    action.onComplete(
                        FoundYouTubeVideo(
                            videoId = "abcdefg",
                            title = "Boclips 4evah",
                            description = "boclips is...interesting",
                            thumbnailUrl = "validurl",
                            playbackId = "1234"
                        )
                    )
                )
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = "<@THAD123> Here are the video details for myvid123:",
                                slackMessageVideos = listOf(
                                    SlackMessageVideo(
                                        imageUrl = "validurl",
                                        title = "Boclips 4evah",
                                        videoId = "abcdefg",
                                        type = YOUTUBE,
                                        playbackId = "1234"
                                    )
                                )
                            )
                        )
                    )
            else ->
                fail<String>("Expected a video retrieval, but got $action")
        }
    }

    @Test
    fun `missing video triggers a chat message with an apology`() {
        when (val action = mentionTerry(
            "video myvid123 doesn't even exist, m8",
            user = "THAD123",
            channel = "#engineering"
        ).action) {
            is VideoRetrieval ->
                assertThat(action.onComplete(MissingVideo(videoId = "myvid123")))
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = """<@THAD123> Sorry, video myvid123 doesn't seem to exist! :("""
                            )
                        )
                    )
            else ->
                fail<String>("Expected a video retrieval, but got $action")
        }
    }

    @Test
    fun `video service server error triggers a chat message with some blame`() {
        when (val action = mentionTerry(
            "please find video thatbreaksvideoservice",
            user = "THAD123",
            channel = "#engineering"
        ).action) {
            is VideoRetrieval ->
                assertThat(action.onComplete(Error(message = "500 REALLY BAD")))
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = """<@THAD123> looks like the video service is broken :("""
                            )
                        )
                    )
            else ->
                fail<String>("Expected a video retrieval, but got $action")
        }
    }

    @Test
    fun `transcript request translates the code into Kaltura language`() {
        assertThat(
            (Terry().receiveSlack(
                BlockActions(
                    actions = listOf(
                        BlockAction(
                            selectedOption = BlockActionSelectedOption("""{"code":"british-english","entryId":"a_Kaltura1D"}""")
                        )
                    ),
                    channel = BlockActionIdentifiable(id = "#legacyOrderDocuments"),
                    user = BlockActionIdentifiable(id = "THAD666"),
                    responseUrl = "https://response.to.me/please"
                )
            ).action as VideoTagging).tag
        ).isEqualTo("caption48british")
    }

    @Test
    fun `transcript request with unknown code gives a malformed rejection`() {
        assertThat(
            Terry().receiveSlack(
                BlockActions(
                    actions = listOf(
                        BlockAction(
                            selectedOption = BlockActionSelectedOption("""{"code":"unknown","entryId":"a_Kaltura1D"}""")
                        )
                    ),
                    channel = BlockActionIdentifiable(id = "#legacyOrderDocuments"),
                    user = BlockActionIdentifiable(id = "THAD666"),
                    responseUrl = "http://www.example.com/"
                )
            ).action
        ).isInstanceOf(MalformedRequestRejection::class.java)
    }

    @Test
    fun `successful transcript request triggers a chat message with the entry ID of the video`() {
        when (val action = Terry().receiveSlack(
            BlockActions(
                actions = listOf(
                    BlockAction(
                        selectedOption = BlockActionSelectedOption("""{"code":"british-english","entryId":"a_Kaltura1D"}""")
                    )
                ),
                channel = BlockActionIdentifiable(id = "#legacyOrderDocuments"),
                user = BlockActionIdentifiable(id = "THAD666"),
                responseUrl = "https://my.response.url"
            )
        ).action) {
            is VideoTagging ->
                assertThat(action.onComplete(Success(entryId = "interviewWithOasis")))
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#legacyOrderDocuments",
                                text = """<@THAD666> OK, I requested a transcript for "interviewWithOasis" (British English)."""
                            )
                        )
                    )
            else ->
                fail<String>("Expected a transcript request, but got $action")
        }
    }

    @Test
    fun `failed transcript request triggers a chat message`() {
        when (val action = Terry().receiveSlack(
            BlockActions(
                actions = listOf(
                    BlockAction(
                        selectedOption = BlockActionSelectedOption("""{"code":"british-english","entryId":"a_Kaltura1D"}""")
                    )
                ),
                channel = BlockActionIdentifiable(id = "#legacyOrderDocuments"),
                user = BlockActionIdentifiable(id = "THAD666"),
                responseUrl = "http://hit.me.up"
            )
        ).action) {
            is VideoTagging ->
                assertThat(action.onComplete(Failure(entryId = "interviewWithBlur", error = "Kaltura fail")))
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#legacyOrderDocuments",
                                text = """<@THAD666> Sorry! I don't think "interviewWithBlur" could be tagged: "Kaltura fail"."""
                            )
                        )
                    )
            else ->
                fail<String>("Expected a transcript request, but got $action")
        }
    }

    private fun mentionTerry(message: String, user: String = "DEFAULTUSERID", channel: String): Decision =
        Terry().receiveSlack(
            EventNotification(
                teamId = irrelevant,
                apiAppId = irrelevant,
                event = AppMention(
                    type = irrelevant,
                    channel = channel,
                    text = "<@TERRYID> $message",
                    eventTs = irrelevant,
                    ts = irrelevant,
                    user = user
                ),
                type = irrelevant,
                authedUsers = emptyList(),
                eventId = irrelevant,
                eventTime = Date()
            )
        )
}
