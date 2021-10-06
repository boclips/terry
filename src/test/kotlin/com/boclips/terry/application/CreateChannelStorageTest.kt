package com.boclips.terry.application

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.boclips.terry.infrastructure.outgoing.policy.FakePolicyRepository
import com.boclips.terry.infrastructure.outgoing.policy.IamPolicyRepository
import com.boclips.terry.infrastructure.outgoing.policy.PolicyRepository
import com.boclips.terry.infrastructure.outgoing.storage.AWSStorageRepository
import com.boclips.terry.infrastructure.outgoing.storage.FakeStorageRepository
import com.boclips.terry.infrastructure.outgoing.storage.StorageRepository
import com.boclips.terry.infrastructure.outgoing.users.FakeUserRepository
import com.boclips.terry.infrastructure.outgoing.users.IamUserRepository
import com.boclips.terry.infrastructure.outgoing.users.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class CreateChannelStorageTest {

    var storageRepository: StorageRepository? = null
    var userRepository: UserRepository? = null
    var policyRepository: PolicyRepository? = null
    var createChannelStorage: CreateChannelStorage? = null

    @Test
    fun `should create channel storage`() {
        val createdChannelStorage = createChannelStorage!!("channel-name") as ChannelCreationSuccess

        assertThat(createdChannelStorage).isInstanceOf(ChannelCreationSuccess::class.java)
        assertThat(createdChannelStorage.storageName).isEqualTo("boclips-upload-channel-name")
        assertThat(createdChannelStorage.userName).isEqualTo("channel-name")
        assertThat(createdChannelStorage.policyName).startsWith("arn:aws:iam::")
        assertThat(createdChannelStorage.policyName).endsWith("boclips-upload-channel-name")
    }

    @Test
    fun `should handle channels that exist already`() {
        val createdChannelStorage = createChannelStorage!!("already-existing-channel") as ChannelAlreadyExists

        assertThat(createdChannelStorage).isInstanceOf(ChannelAlreadyExists::class.java)
    }
}

class CreateAWSStorageTest : CreateChannelStorageTest() {
    @BeforeEach
    fun setUp() {
        val s3Client = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.EU_WEST_1)
            .withCredentials(EnvironmentVariableCredentialsProvider())
            .build()

        val notificationService = TODO()

        storageRepository = AWSStorageRepository(s3Client, notificationService)
        (storageRepository as AWSStorageRepository).create("already-existing-channel")
        storageRepository!!.delete("channel-name")
        userRepository = IamUserRepository()
        policyRepository = IamPolicyRepository()

        createChannelStorage = CreateChannelStorage(storageRepository!!, policyRepository!!, userRepository!!)
    }
}

class CreateFakeStorageTest : CreateChannelStorageTest() {
    @BeforeEach
    fun setUp() {
        storageRepository = FakeStorageRepository()
        (storageRepository as FakeStorageRepository).create("already-existing-channel")
        userRepository = FakeUserRepository()
        policyRepository = FakePolicyRepository()
        createChannelStorage = CreateChannelStorage(storageRepository!!, policyRepository!!, userRepository!!)
    }
}
