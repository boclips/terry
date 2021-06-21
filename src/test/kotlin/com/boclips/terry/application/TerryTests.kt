package com.boclips.terry.application

import com.boclips.terry.infrastructure.incoming.*
import com.boclips.terry.infrastructure.outgoing.securecredentials.CredentialNotFound
import com.boclips.terry.infrastructure.outgoing.securecredentials.SafenoteFailure
import com.boclips.terry.infrastructure.outgoing.securecredentials.SecureCredential
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
import java.util.*

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
        val decision = mentionTerry("hi Tezza", user = "UBS7V80PR", channel = "#engineering")
        val reply = decision.action as ChatReply
        assertThat(reply.slackMessage.channel).isEqualTo("#engineering")
        assertThat(reply.slackMessage.text).startsWith("<@UBS7V80PR> Some things you can do:")
        assertThat(decision.log).contains("Some things you can do")
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
    fun `can retrieve credentials for a channel`() {
        val decision = mentionTerry(
            "SAfeNote for mythology-and-fiction_explained please bud?",
            user = "UBS7V80PR",
            channel = "#engineering"
        )
        assertThat(decision.log).isEqualTo("Retrieving safenote for mythology-and-fiction_explained")
        when (val response = decision.action) {
            is ChannelUploadCredentialRetrieval -> {
                assertThat(response.channelName).isEqualTo(
                    "mythology-and-fiction_explained"
                )
            }
            else ->
                fail<String>("Expected a credential retrieval, but got $response")
        }
    }

    @Test
    fun `can retrieve credentials for a channel name delimited by 'for' and end-of-line`() {
        val decision = mentionTerry(
            "can I get a new safenote for The Business Professor",
            user = "UBS7V80PR",
            channel = "#engineering"
        )
        assertThat(decision.log).isEqualTo("Retrieving safenote for the-business-professor")
        when (val response = decision.action) {
            is ChannelUploadCredentialRetrieval -> {
                assertThat(response.channelName).isEqualTo(
                    "the-business-professor"
                )
            }
            else ->
                fail<String>("Expected a credential retrieval, but got $response")
        }
    }

    @Test
    fun `can retrieve credentials for a channel name delimited by 'for' and 'please'`() {
        val decision = mentionTerry(
            "can I get a new safenote for The Business Professor please my good chumster?",
            user = "UBS7V80PR",
            channel = "#engineering"
        )
        assertThat(decision.log).isEqualTo("Retrieving safenote for the-business-professor")
        when (val response = decision.action) {
            is ChannelUploadCredentialRetrieval -> {
                assertThat(response.channelName).isEqualTo(
                    "the-business-professor"
                )
            }
            else ->
                fail<String>("Expected a credential retrieval, but got $response")
        }
    }

    @Test
    fun `when credentials are retrieved successfully reply with a credential link`() {
        when (val action = mentionTerry(
            "safenote for mythology-and-fiction_explained",
            user = "THAD123",
            channel = "#engineering"
        ).action) {
            is ChannelUploadCredentialRetrieval ->
                assertThat(
                    action.onComplete(
                        SecureCredential(url = "https://example.com/mythology")
                    )
                )
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = "Sure <@THAD123>, here you go: https://example.com/mythology",
                            )
                        )
                    )
            else ->
                fail<String>("Expected a channel upload credential retrieval, but got $action")
        }
    }

    @Test
    fun `when credentials are not found tell user`() {
        when (val action = mentionTerry(
            "safenote for mythology-and-fiction_explained",
            user = "THAD123",
            channel = "#engineering"
        ).action) {
            is ChannelUploadCredentialRetrieval ->
                assertThat(
                    action.onComplete(
                        CredentialNotFound
                    )
                )
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = "Sorry <@THAD123>, I can't find that channel! Maybe check the name?",
                            )
                        )
                    )
            else ->
                fail<String>("Expected a channel upload credential retrieval, but got $action")
        }
    }

    @Test
    fun `when Safenote fails tell user`() {
        when (val action = mentionTerry(
            "safenote for mythology-and-fiction_explained",
            user = "THAD123",
            channel = "#engineering"
        ).action) {
            is ChannelUploadCredentialRetrieval ->
                assertThat(
                    action.onComplete(
                        SafenoteFailure("looks like Safenote is down!")
                    )
                )
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = "Sorry <@THAD123>, the Safenote service isn't working! Ask an engineer? (looks like Safenote is down!)",
                            )
                        )
                    )
            else ->
                fail<String>("Expected a channel upload credential retrieval, but got $action")
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
