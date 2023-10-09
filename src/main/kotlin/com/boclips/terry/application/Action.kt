package com.boclips.terry.application

import com.boclips.terry.infrastructure.incoming.RawSlackRequest
import com.boclips.terry.infrastructure.outgoing.securecredentials.SecureCredentialResponse
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.transcripts.TaggingResponse
import com.boclips.terry.infrastructure.outgoing.videos.VideoServiceResponse

sealed class Action

data class AuthenticityRejection(
    val request: RawSlackRequest,
    val reason: String
) : Action()

object MalformedRequestRejection : Action()
data class VerificationResponse(val challenge: String) : Action()
data class ChatReply(val slackMessage: SlackMessage) : Action()
data class VideoRetrieval(
    val videoId: String,
    val onComplete: (VideoServiceResponse) -> ChatReply
) : Action()

data class ChannelUploadCredentialRetrieval(
    val channelName: String,
    val onComplete: (SecureCredentialResponse) -> ChatReply
) : Action()

data class VideoTagging(
    val entryId: String,
    val tag: String,
    val onComplete: (TaggingResponse) -> ChatReply,
    val responseUrl: String
) : Action()

data class ChannelCreation(
    val channelName: String,
    val onComplete: (ChannelCreationResponse) -> ChatReply
) : Action()

data class SentryReportCreation(
    val params: SentryReportParams,
    val onComplete: (SentryReportResponse) -> ChatReply
) : Action()
