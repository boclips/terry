package com.boclips.terry.application

import com.boclips.terry.infrastructure.outgoing.policy.PolicyRepository
import com.boclips.terry.infrastructure.outgoing.storage.*
import com.boclips.terry.infrastructure.outgoing.users.UserCreated
import com.boclips.terry.infrastructure.outgoing.users.UserRepository

class CreateChannelStorage(
    val storageRepository: StorageRepository,
    val policyRepository: PolicyRepository,
    val userRepository: UserRepository
) {

    operator fun invoke(name: String): ChannelCreationResponse {
        storageRepository.create(name).let { storageCreationResult ->
            return when (storageCreationResult) {
                is StorageCreationSuccess -> {
                    val storageName = storageCreationResult.name

                    userRepository.create(name).let {
                        val username = (it as UserCreated).username
                        policyRepository.create(storageName)?.let { policyId ->
                            userRepository.addPolicyToUser(username, policyId)
                            ChannelCreationSuccess(
                                storageName = storageName,
                                userName = username,
                                policyName = policyId
                            )
                        } ?: InvalidPolicyName
                    }
                }
                is InvalidName -> {
                    InvalidChannelName
                }
                is StorageAlreadyExists ->
                    ChannelAlreadyExists
                is StorageCreationFailure -> ChannelCreationFailed
            }
        }
    }
}
