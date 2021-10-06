package com.boclips.terry.config

import com.boclips.terry.application.CreateChannelStorage
import com.boclips.terry.infrastructure.outgoing.policy.FakePolicyRepository
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
class ChannelConfig(
    private val storageRepository: StorageRepository,
    private val userRepository: UserRepository,
    private val policyRepository: PolicyRepository
) {
    @Bean
    fun createChannel(): CreateChannelStorage = CreateChannelStorage(
        storageRepository = storageRepository,
        userRepository = userRepository,
        policyRepository = policyRepository
    )
}
