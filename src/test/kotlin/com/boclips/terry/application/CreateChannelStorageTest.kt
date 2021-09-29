package com.boclips.terry.application

import com.boclips.terry.infrastructure.outgoing.storage.FakeStorageRepository
import com.boclips.terry.infrastructure.outgoing.users.FakeUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateChannelStorageTest {
    val storageRepository = FakeStorageRepository()
    val userRepository = FakeUserRepository()
    val createChannelStorage = CreateChannelStorage(storageRepository, userRepository)

    @Test
    fun `should create channel storage`() {
        val createdChannelStorage = createChannelStorage("channel-name")

        assertThat(createdChannelStorage).isEqualTo(ChannelCreationSuccess("boclips-upload-channel-name"))
        assertThat(storageRepository.exists("boclips-upload-channel-name")).isTrue
        assertThat(userRepository.exists("channel-name")).isTrue
    }
}