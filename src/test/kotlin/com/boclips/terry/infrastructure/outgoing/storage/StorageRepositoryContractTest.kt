package com.boclips.terry.infrastructure.outgoing.storage

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.boclips.terry.config.AWSNotificationProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class StorageRepositoryTest {
    var storageRepository: StorageRepository? = null
    var newBucketName: String? = null

    @Test
    fun `it returns a successful creation response when successful`() {
        assertThat(storageRepository!!.create(newBucketName!!))
            .isEqualTo(StorageCreationSuccess(name = "boclips-upload-$newBucketName"))
    }

    @Test
    fun `it returns a failure about the user when username is invalid`() {
        assertThat(storageRepository!!.create("cannothave!exclamation"))
            .isEqualTo(InvalidName)
    }

    @Test
    fun `it returns a already exists error when bucket exists`() {
        assertThat(storageRepository!!.create("existing-channel-name"))
            .isEqualTo(StorageAlreadyExists)
    }

    @Test
    fun `cannot delete non existent buckets`() {
        val name = "channel-bucket-name-that-doesnt-exist-and-will-never-exist"
        val deletionResponse = storageRepository!!.delete(name)
        assertThat(deletionResponse).isInstanceOf(StorageDeletionFailed::class.java)
    }
}

class AWSStorageRepositoryContractTest : StorageRepositoryTest() {
    @BeforeEach
    fun setUp() {
        val s3Client = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.EU_WEST_1)
            .withCredentials(EnvironmentVariableCredentialsProvider())
            .build()

        storageRepository = AWSStorageRepository(s3Client, AWSNotificationService(s3Client, AWSNotificationProperties().apply { this.channelTopicArn = "fake" }))
        newBucketName = "test-test-testing"
        storageRepository!!.delete(newBucketName!!)
    }


}

class FakeStorageRepositoryContractTest : StorageRepositoryTest() {
    @BeforeEach
    fun setUp() {
        storageRepository = FakeStorageRepository()
        newBucketName = "test-test-testing"
        storageRepository!!.delete(newBucketName!!)
        storageRepository!!.create("existing-channel-name")
    }
}
