package com.boclips.terry.config

import com.boclips.terry.infrastructure.outgoing.storage.AWSStorageRepository
import com.boclips.terry.infrastructure.outgoing.storage.StorageRepository
import com.boclips.terry.infrastructure.outgoing.users.IamUserRepository
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
class ChannelConfig {
    fun channelRepository() : StorageRepository = AWSStorageRepository()
}
