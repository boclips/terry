package com.boclips.terry.infrastructure.outgoing.videos

import com.boclips.videos.api.httpclient.VideosClient
    import feign.FeignException
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException

@Component
class HTTPVideoService(private val videosClient: VideosClient) : VideoService {
    companion object : KLogging()
    override fun get(videoId: String): VideoServiceResponse {
        return try {
            logger.debug { "Fetching video with id $videoId" }
            val video = videosClient.getVideo(videoId = videoId)
            when (video.playback?.type) {
                "STREAM" ->
                    FoundKalturaVideo(
                        videoId = video.id!!,
                        title = video.title!!,
                        description = video.description!!,
                        thumbnailUrl = video.playback?._links!!["thumbnail"]?.href ?: "",
                        playbackId = video.playback?.id!!,
                        streamUrl = video.playback?._links!!["hlsStream"]?.href
                    )
                "YOUTUBE" ->
                    FoundYouTubeVideo(
                        videoId = video.id!!,
                        title = video.title!!,
                        description = video.description!!,
                        thumbnailUrl = video.playback?._links!!["thumbnail"]?.href ?: "",
                        playbackId = video.playback?.id!!
                    )
                else ->
                    MissingVideo(videoId = videoId)
            }
        } catch (ex: FeignException) {
            if (ex.status() == 404) {
                logger.error { "$videoId is missing" }
                MissingVideo(videoId)
            } else {
                logger.error { "Error fetching video $videoId: ${ex.message}" }
                Error("Something went wrong fetching video with id $videoId - ${ex.message}")
            }
        } catch (ex: HttpServerErrorException) {
            logger.error { "Error fetching video $videoId: ${ex}" }
            Error(message = "Server bad: $ex")
        }
    }
}
