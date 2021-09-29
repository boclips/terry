package com.boclips.terry.application

import com.boclips.terry.infrastructure.outgoing.channels.ChannelCreationResponse
import com.boclips.terry.infrastructure.outgoing.channels.ChannelCreationSuccess
import com.boclips.terry.infrastructure.outgoing.storage.StorageCreationSuccess
import com.boclips.terry.infrastructure.outgoing.storage.StorageRepository
import com.boclips.terry.infrastructure.outgoing.users.UserRepository

class CreateChannelStorage(
    val storageRepository: StorageRepository,
    val userRepository: UserRepository
) {

    operator fun invoke(name: String): ChannelCreationResponse {
        val createdStorage = storageRepository.create(name)
        userRepository.create(name)

        return ChannelCreationSuccess((createdStorage as? StorageCreationSuccess)!!.name)
    }
}