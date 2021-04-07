package com.boclips.terry.infrastructure.outgoing.videos

sealed class VideoServiceResponse

interface FoundVideo {
    val videoId: String
    val title: String
    val description: String
    val thumbnailUrl: String
    val playbackId: String?
    fun getType(): String
}

data class MissingVideo(val videoId: String) : VideoServiceResponse()
data class Error(val message: String) : VideoServiceResponse()

data class FoundKalturaVideo(
    override val videoId: String,
    override val title: String,
    override val description: String,
    override val thumbnailUrl: String,
    override val playbackId: String?,
    val streamUrl: String?
) : FoundVideo, VideoServiceResponse() {
    override fun getType(): String = "Kaltura"
}

data class FoundYouTubeVideo(
    override val videoId: String,
    override val title: String,
    override val description: String,
    override val thumbnailUrl: String,
    override val playbackId: String
) : FoundVideo, VideoServiceResponse() {
    override fun getType(): String = "Youtube"
}
