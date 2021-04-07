package com.boclips.terry.presentation

import com.boclips.kalturaclient.KalturaClient
import com.boclips.terry.application.AuthenticityRejection
import com.boclips.terry.application.ChatReply
import com.boclips.terry.application.MalformedRequestRejection
import com.boclips.terry.application.Terry
import com.boclips.terry.application.VerificationResponse
import com.boclips.terry.application.VideoRetrieval
import com.boclips.terry.application.VideoTagging
import com.boclips.terry.infrastructure.incoming.Malformed
import com.boclips.terry.infrastructure.incoming.SlackRequest
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import com.boclips.terry.infrastructure.outgoing.videos.VideoService
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
    private val slackPoster: SlackPoster,
    private val videoService: VideoService,
    private val kalturaClient: KalturaClient,
    private val objectMapper: ObjectMapper
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

    private fun handleSlack(body: SlackRequest): ResponseEntity<ControllerResponse> {
        return when (val action = terry.receiveSlack(body).action) {
            is AuthenticityRejection ->
                unauthorized()
                    .also {
                        logger.error { action.reason }
                        logger.error { action.request.body }
                    }
            MalformedRequestRejection ->
                badRequest()
            is ChatReply ->
                ok()
                    .also { chat(action) }
            is VideoRetrieval ->
                ok()
                    .also { getVideo(action) }
            is VerificationResponse ->
                ok(SlackVerificationResponse(action.challenge))
            is VideoTagging ->
                ok()
                    .also { tagVideo(action) }
        }
    }

    private fun tagVideo(action: VideoTagging) {
        SlackControllerJobs(
            slackPoster = slackPoster,
            videoService = videoService,
            kalturaClient = kalturaClient
        )
            .tagVideo(action)
    }

    private fun getVideo(action: VideoRetrieval) {
        SlackControllerJobs(
            slackPoster = slackPoster,
            videoService = videoService,
            kalturaClient = kalturaClient
        )
            .getVideo(action)
    }

    private fun chat(action: ChatReply) {
        SlackControllerJobs(
            slackPoster = slackPoster,
            videoService = videoService,
            kalturaClient = kalturaClient
        )
            .chat(action.slackMessage, "https://slack.com/api/chat.postMessage")
    }

    private fun ok(obj: ControllerResponse = Success) =
        ResponseEntity(obj, HttpStatus.OK)

    private fun badRequest(): ResponseEntity<ControllerResponse> =
        ResponseEntity(Failure, HttpStatus.BAD_REQUEST)

    private fun unauthorized(): ResponseEntity<ControllerResponse> =
        ResponseEntity(Failure, HttpStatus.UNAUTHORIZED)
}
