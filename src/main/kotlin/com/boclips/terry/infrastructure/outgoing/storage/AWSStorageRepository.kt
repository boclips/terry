package com.boclips.terry.infrastructure.outgoing.storage

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.IllegalBucketNameException

class AWSStorageRepository(
    private val s3: AmazonS3,
    private val notificationService: NotificationService
) : StorageRepository {
    override fun create(name: String): StorageCreationResponse {
        val bucketName = bucketName(name)
        try {
            s3.createBucket(bucketName)
        } catch (e: IllegalBucketNameException) {
            return InvalidName
        } catch (e: AmazonS3Exception) {
            return if (e.errorCode == "BucketAlreadyOwnedByYou") {
                StorageAlreadyExists
            } else {
                StorageCreationFailure(e.message ?: "Storage creation failed!")
            }
        }

        notificationService.addBucketNotification(bucketName)

        return StorageCreationSuccess(bucketName)
    }

    override fun delete(name: String): StorageDeletionResponse {
        try {
            s3.deleteBucket(bucketName(name))
        } catch (ex: Exception) {
            return StorageDeletionFailed
        }
        return StorageDeletionSuccess
    }

    private fun bucketName(name: String) = "boclips-upload-$name"
}
