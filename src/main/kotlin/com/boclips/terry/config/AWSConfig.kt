package com.boclips.terry.config

import com.boclips.terry.application.CreateChannelStorage
import com.boclips.terry.infrastructure.outgoing.policy.IamPolicyRepository
import com.boclips.terry.infrastructure.outgoing.policy.PolicyRepository
import com.boclips.terry.infrastructure.outgoing.storage.AWSStorageRepository
import com.boclips.terry.infrastructure.outgoing.storage.StorageRepository
import com.boclips.terry.infrastructure.outgoing.users.IamUserRepository
import com.boclips.terry.infrastructure.outgoing.users.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
class AWSConfig {
    fun channelRepository(): StorageRepository = AWSStorageRepository()
    fun policyRepository(): PolicyRepository = IamPolicyRepository()
    fun userRepository(): UserRepository = IamUserRepository()

    @Bean
    fun createChannel(): CreateChannelStorage = CreateChannelStorage(
        storageRepository = channelRepository(),
        userRepository = userRepository(),
        policyRepository = policyRepository()
    )
}
