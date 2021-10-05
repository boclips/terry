package com.boclips.terry.infrastructure.outgoing.storage


class FakeStorageRepository : StorageRepository {
    private val storageNames = mutableListOf<String>()

    override fun create(name: String): StorageCreationResponse =
        if (name.contains("!")) {
            InvalidName
        } else if (storageNames.contains(getBucketName(name))) {
            StorageAlreadyExists
        } else {
            val bucketName = getBucketName(name)
            storageNames.add(bucketName)
            StorageCreationSuccess(name = bucketName)
        }

    private fun getBucketName(name: String): String {
        return "boclips-upload-$name"
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
