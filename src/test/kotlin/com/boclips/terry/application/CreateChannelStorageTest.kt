package com.boclips.terry.application

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
        assertThat(createdChannelStorage.userName).isEqualTo("boclips-upload-channel-name")
        assertThat(createdChannelStorage.policyName).isEqualTo("boclips-upload-channel-name")
    }

    @Test
    fun `should handle channels that exist already`() {
        val createdChannelStorage = createChannelStorage!!("channel-name") as ChannelAlreadyExists

        assertThat(createdChannelStorage).isInstanceOf(ChannelAlreadyExists::class.java)
    }
}
class CreateAWSStorageTest : CreateChannelStorageTest() {
    @BeforeEach
    fun setUp () {
        storageRepository = AWSStorageRepository()
        userRepository = IamUserRepository()
        policyRepository = IamPolicyRepository()
        createChannelStorage = CreateChannelStorage(storageRepository!!, policyRepository!!, userRepository!!)
    }
}

class CreateFakeStorageTest : CreateChannelStorageTest() {
    @BeforeEach
    fun setUp () {
        storageRepository = FakeStorageRepository()
        userRepository = FakeUserRepository()
        policyRepository = FakePolicyRepository()
    }
}
