package com.boclips.terry.infrastructure.outgoing.videos

interface VideoService {
    fun get(videoId: String): VideoServiceResponse
}
