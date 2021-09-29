package com.boclips.terry.infrastructure.outgoing.storage

import com.boclips.terry.infrastructure.outgoing.channels.ChannelDeletionResponse

interface StorageRepository {
    fun create(name: String): StorageCreationResponse
    fun delete(name: String): ChannelDeletionResponse
}
