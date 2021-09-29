package com.boclips.terry.infrastructure.outgoing.storage


class FakeStorageRepository : StorageRepository {
    private val storageNames = mutableListOf<String>()

    override fun create(name: String): StorageCreationResponse =
        if (name.contains("!")) {
            InvalidName
        } else {
            val bucketName = "boclips-upload-$name"
            storageNames.add(bucketName)
            StorageCreationSuccess(name = bucketName)
        }

    override fun delete(name: String): StorageDeletionResponse {
        if (exists(name)) {
            storageNames.remove(name)
            return StorageDeletionSuccess
        }
        return StorageDeletionFailed
    }

    fun exists(name: String) = storageNames.contains(name)
}
