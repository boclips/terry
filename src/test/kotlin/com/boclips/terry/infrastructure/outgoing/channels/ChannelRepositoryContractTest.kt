package com.boclips.terry.infrastructure.outgoing.channels

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.boclips.terry.infrastructure.outgoing.storage.AWSStorageRepository
import com.boclips.terry.infrastructure.outgoing.storage.FakeStorageRepository
import com.boclips.terry.infrastructure.outgoing.storage.StorageRepository
import com.boclips.terry.infrastructure.outgoing.users.IamUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class StorageRepositoryTest {
    var storageRepository: StorageRepository? = null

    @Test
    fun `it returns a successful creation response when successful`() {
        assertThat(storageRepository!!.delete("test-test-test")).
            isEqualTo(ChannelDeletionSuccess)
        assertThat(storageRepository!!.create("test-test-test"))
            .isEqualTo(ChannelCreationSuccess(storageName = "test-test-test"))
    }

    @Test
    fun `it returns a failure about the user when username is invalid`() {
        assertThat(storageRepository!!.create("cannothave!exclamation"))
            .isEqualTo(InvalidName)
    }
}

class AWSStorageRepositoryTest : StorageRepositoryTest() {
    @BeforeEach
    internal fun setUp() {
        storageRepository = AWSStorageRepository()
    }

    @Test
    fun `creates user and returns for the bucket`() {
        val channelName = "channel-name1"
        val creationResponse = storageRepository!!.create(channelName)

        assertThat((creationResponse as ChannelCreationSuccess).storageName).isEqualTo(channelName)
    }

    @Test
    fun `the user can access the bucket`() {
        val s3 = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.EU_WEST_1)
            .withCredentials(EnvironmentVariableCredentialsProvider())
            .build()

//        val iam = AmazonIdentityManagementAsyncClientBuilder
//            .standard()
//            .withRegion(Regions.EU_WEST_1)
//            .withCredentials()
    }

    @Test
    fun `the user cannot access other buckets`() {
    }
}

class FakeStorageRepositoryTest : StorageRepositoryTest() {
    @BeforeEach
    internal fun setUp() {
        storageRepository = FakeStorageRepository()
    }
}
