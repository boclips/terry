package com.boclips.terry.infrastructure.outgoing.storage

import com.boclips.terry.infrastructure.outgoing.channels.*

class FakeStorageRepository : StorageRepository {
    val channels = mutableListOf<String>()

    override fun create(name: String): StorageCreationResponse =
        if (name.contains("!")) {
            InvalidName
        } else {
            val bucketName = "boclips-upload-$name"
            channels.add(bucketName)
            StorageCreationSuccess(name = bucketName)
        }

    override fun delete(name: String): ChannelDeletionResponse {
        if (exists(name)) {
            channels.remove(name)
            return ChannelDeletionSuccess
        }
        return ChannelDeletionFailed
    }

    fun exists(name: String) = channels.contains(name)
}
