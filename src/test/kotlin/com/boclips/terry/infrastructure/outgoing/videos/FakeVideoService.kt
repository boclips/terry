package com.boclips.terry.infrastructure.outgoing.videos

import com.boclips.terry.Fake

class FakeVideoService : Fake, VideoService {
    var lastIdRequest: String? = null
    var nextResponse: VideoServiceResponse? = null

    init {
        reset()
    }

    override fun reset(): Fake = this
        .also { lastIdRequest = null }

    override fun get(videoId: String): VideoServiceResponse = nextResponse!!
        .also { lastIdRequest = videoId }

    fun respondWith(response: VideoServiceResponse): FakeVideoService = this
        .also { nextResponse = response }
}
