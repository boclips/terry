package com.boclips.terry.infrastructure.outgoing.channels

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.IllegalBucketNameException

class AWSChannelRepository : ChannelRepository {
    val s3 = AmazonS3ClientBuilder
        .standard()
        .withRegion(Regions.EU_WEST_1)
        .withCredentials(EnvironmentVariableCredentialsProvider())
        .build()

    override fun create(name: String): ChannelCreationResponse {
        try {
            s3.createBucket(bucketName(name))
        } catch (e: IllegalBucketNameException) {
            return InvalidName
        }

        return ChannelCreationSuccess
    }

    override fun delete(name: String): ChannelDeletionResponse {
        s3.deleteBucket(bucketName(name))
        return ChannelDeletionSuccess
    }

    private fun bucketName(name: String) = "boclips-upload-$name"
}
