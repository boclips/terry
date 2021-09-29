package com.boclips.terry.infrastructure.outgoing.storage

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.IllegalBucketNameException

class AWSStorageRepository : StorageRepository {
    private val s3 = AmazonS3ClientBuilder
        .standard()
        .withRegion(Regions.EU_WEST_1)
        .withCredentials(EnvironmentVariableCredentialsProvider())
        .build()

    override fun create(name: String): StorageCreationResponse {
        val bucketName = bucketName(name)
        try {
            s3.createBucket(bucketName)
        } catch (e: IllegalBucketNameException) {
            return InvalidName
        }

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
