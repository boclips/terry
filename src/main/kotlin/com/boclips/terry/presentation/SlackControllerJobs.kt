package com.boclips.terry.presentation

import com.boclips.kalturaclient.KalturaClient
import com.boclips.terry.application.ChannelUploadCredentialRetrieval
import com.boclips.terry.application.VideoRetrieval
import com.boclips.terry.application.VideoTagging
import com.boclips.terry.infrastructure.outgoing.securecredentials.SecureCredentialRetriever
import com.boclips.terry.infrastructure.outgoing.slack.PostFailure
import com.boclips.terry.infrastructure.outgoing.slack.PostSuccess
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import com.boclips.terry.infrastructure.outgoing.transcripts.Success
import com.boclips.terry.infrastructure.outgoing.videos.VideoService
import org.springframework.scheduling.annotation.Async

open class SlackControllerJobs(
    private val slackPoster: SlackPoster,
    private val videoService: VideoService,
    private val kalturaClient: KalturaClient,
    private val retriever: SecureCredentialRetriever
) {
    @Async
    open fun getVideo(action: VideoRetrieval) {
        action
            .onComplete(videoService.get(action.videoId))
            .apply { chat(slackMessage) }
    }

    @Async
    open fun tagVideo(action: VideoTagging) {
        kalturaClient.tag(action.entryId, listOf(action.tag))
        chat(
            action.onComplete(
                Success(entryId = action.entryId)
            ).slackMessage,
            action.responseUrl
        )
    }

    @Async
    fun getCredential(action: ChannelUploadCredentialRetrieval) {
        action
            .onComplete(retriever.get(action.channelName))
            .apply { chat(slackMessage) }
    }

    @Async
    open fun chat(slackMessage: SlackMessage, url: String = "https://slack.com/api/chat.postMessage"): Unit =
        when (val response = slackPoster.chatPostMessage(slackMessage, url = url)) {
            is PostSuccess ->
                SlackController.logger.debug { "Successful post of $slackMessage" }
            is PostFailure ->
                SlackController.logger.error { "Failed post to Slack: ${response.message}" }
        }
}
