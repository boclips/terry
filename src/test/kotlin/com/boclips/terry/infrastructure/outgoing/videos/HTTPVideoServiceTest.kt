package com.boclips.terry.infrastructure.outgoing.videos

import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.request.video.YoutubePlaybackResource
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.video.VideoResource
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import feign.FeignException.NotFound
import feign.Request
import feign.Request.HttpMethod
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HTTPVideoServiceTest {

    @Test
    fun `gets a Kaltura video`() {
        val videosClient: VideosClient = mock()
        val httpVideoService = HTTPVideoService(videosClient)

        val videoId = "avenue-video"
        whenever(videosClient.getVideo(videoId))
            .thenReturn(
                VideoResource(
                    id = videoId,
                    title = "engine",
                    description = "engine is sturdy",
                    playback = StreamPlaybackResource(
                        id = "lzzI",
                        _links = mapOf(
                            "thumbnail" to HateoasLink("https://avenue-video/thumbnail"),
                            "hlsStream" to HateoasLink("https://avenue-video/stream")
                        ),
                        referenceId = null
                    ),
                    _links = null
                )
            )

        val foundVideo = httpVideoService.get(videoId) as FoundKalturaVideo

        assertThat(foundVideo.videoId).isEqualTo(videoId)
        assertThat(foundVideo.title).isEqualTo("engine")
        assertThat(foundVideo.description).isEqualTo("engine is sturdy")
        assertThat(foundVideo.thumbnailUrl).isEqualTo("https://avenue-video/thumbnail")
        assertThat(foundVideo.streamUrl)
            .startsWith("https://avenue-video/stream")
        assertThat(foundVideo.playbackId).isEqualTo("lzzI")
    }

    @Test
    fun `gets a youtube video`() {
        val videosClient: VideosClient = mock()
        val httpVideoService = HTTPVideoService(videosClient)

        val videoId = "avenue-video"
        whenever(videosClient.getVideo(videoId))
            .thenReturn(
                VideoResource(
                    id = videoId,
                    title = "engine",
                    description = "engine is sturdy",
                    playback = YoutubePlaybackResource(
                        id = "lzzI",
                        _links = mapOf(
                            "thumbnail" to HateoasLink("https://avenue-video/thumbnail")
                        ),
                    ),
                    _links = null
                )
            )

        val foundVideo = httpVideoService.get(videoId) as FoundYouTubeVideo

        assertThat(foundVideo.videoId).isEqualTo(videoId)
        assertThat(foundVideo.title).isEqualTo("engine")
        assertThat(foundVideo.description).isEqualTo("engine is sturdy")
        assertThat(foundVideo.thumbnailUrl).isEqualTo("https://avenue-video/thumbnail")
        assertThat(foundVideo.playbackId).isEqualTo("lzzI")
    }

    @Test
    fun `gives MissingVideo error when playack information is absent`() {
        val videosClient: VideosClient = mock()
        val httpVideoService = HTTPVideoService(videosClient)

        val videoId = "bad video"
        whenever(videosClient.getVideo(videoId))
            .thenReturn(
                VideoResource(
                    id = videoId,
                    title = "engine",
                    description = "engine is sturdy",
                    playback = null,
                    _links = null
                )
            )

        val missingVideo = httpVideoService.get(videoId) as MissingVideo
        assertThat(missingVideo.videoId).isEqualTo(videoId)
    }
}
