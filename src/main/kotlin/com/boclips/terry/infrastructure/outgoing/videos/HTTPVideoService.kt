package com.boclips.terry.infrastructure.outgoing.videos

import com.boclips.videos.api.httpclient.VideosClient
import feign.FeignException
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException

@Component
class HTTPVideoService(private val videosClient: VideosClient) : VideoService {
    override fun get(videoId: String): VideoServiceResponse {
        return try {
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
                MissingVideo(videoId)
            } else {
                Error("Something went wrong fetching video with id $videoId - ${ex.message}")
            }
        } catch (ex: HttpServerErrorException) {
            Error(message = "Server bad: $ex")
        }
    }
}
