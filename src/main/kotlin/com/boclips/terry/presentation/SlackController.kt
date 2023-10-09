package com.boclips.terry.presentation

import com.boclips.terry.application.AuthenticityRejection
import com.boclips.terry.application.ChannelCreation
import com.boclips.terry.application.ChannelUploadCredentialRetrieval
import com.boclips.terry.application.ChatReply
import com.boclips.terry.application.MalformedRequestRejection
import com.boclips.terry.application.SentryReportCreation
import com.boclips.terry.application.Terry
import com.boclips.terry.application.VerificationResponse
import com.boclips.terry.application.VideoRetrieval
import com.boclips.terry.application.VideoTagging
import com.boclips.terry.infrastructure.incoming.Malformed
import com.boclips.terry.infrastructure.incoming.SlackRequest
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URLDecoder
import java.nio.charset.Charset
import javax.servlet.http.HttpServletRequest

@RestController
class SlackController(
    private val terry: Terry,
    private val objectMapper: ObjectMapper,
    private val slackControllerJobs: SlackControllerJobs
) {
    companion object : KLogging()

    @GetMapping("/")
    fun index() = "<h1>Do as I say, and do not do as I do</h1>"

    @PostMapping("/slack")
    fun slack(@RequestBody request: SlackRequest): ResponseEntity<ControllerResponse> =
        handleSlack(request)

    @PostMapping("/slack-interaction")
    fun slackInteraction(request: HttpServletRequest): ResponseEntity<ControllerResponse> =
        handleSlack(
            hydrate(
                URLDecoder.decode(
                    request.reader.readText().substringAfter("payload="),
                    Charset.defaultCharset().toString()
                )
            )
        )

    private fun hydrate(payload: String): SlackRequest =
        try {
            objectMapper.readValue(payload, SlackRequest::class.java)
        } catch (e: Exception) {
            logger.error { e.message }
            Malformed
        }

    private fun handleSlack(request: SlackRequest): ResponseEntity<ControllerResponse> {
        logger.info { "Handling slack request: $request" }
        return when (val action = terry.receiveSlack(request).action) {
            is AuthenticityRejection ->
                unauthorized()
                    .also {
                        logger.error { action.reason }
                        logger.error { action.request.body }
                    }

            is MalformedRequestRejection -> badRequest()
            is ChatReply -> ok().also { chat(action) }
            is VideoRetrieval -> ok().also { getVideo(action) }
            is VerificationResponse -> ok(SlackVerificationResponse(action.challenge))
            is VideoTagging -> ok().also { tagVideo(action) }
            is ChannelUploadCredentialRetrieval -> ok().also { getCredential(action) }
            is ChannelCreation -> ok().also { createChannelBucket(action) }
            is SentryReportCreation -> ok().also { createSentryReport(action) }
        }
    }

    private fun getCredential(action: ChannelUploadCredentialRetrieval) {
        slackControllerJobs.getCredential(action)
    }

    private fun tagVideo(action: VideoTagging) {
        slackControllerJobs.tagVideo(action)
    }

    private fun getVideo(action: VideoRetrieval) {
        slackControllerJobs.getVideo(action)
    }

    private fun createChannelBucket(action: ChannelCreation) {
        slackControllerJobs.createChannelBucket(action)
    }

    private fun createSentryReport(action: SentryReportCreation) {
        slackControllerJobs.createSentryReport(action)
    }

    private fun chat(action: ChatReply) {
        slackControllerJobs.chat(action.slackMessage)
    }

    private fun ok(obj: ControllerResponse = Success) =
        ResponseEntity(obj, HttpStatus.OK)

    private fun badRequest(): ResponseEntity<ControllerResponse> =
        ResponseEntity(Failure, HttpStatus.BAD_REQUEST)

    private fun unauthorized(): ResponseEntity<ControllerResponse> =
        ResponseEntity(Failure, HttpStatus.UNAUTHORIZED)
}
