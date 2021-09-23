package com.boclips.terry.infrastructure.outgoing.channels

interface ChannelRepository {
    fun create(name: String): ChannelCreationResponse
    fun delete(name: String): ChannelDeletionResponse
}
