package com.boclips.terry.infrastructure.outgoing.storage

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

class AWSStorageRepositoryTest : StorageRepositoryTest() {
    @BeforeEach
    fun setUp() {
        storageRepository = AWSStorageRepository()
        newBucketName = "test-test-testing"
        storageRepository!!.delete(newBucketName!!)
    }
}

class FakeStorageRepositoryTest : StorageRepositoryTest() {
    @BeforeEach
    fun setUp() {
        storageRepository = FakeStorageRepository()
        newBucketName = "test-test-testing"
        storageRepository!!.delete(newBucketName!!)
        storageRepository!!.create("existing-channel-name")
    }
}
