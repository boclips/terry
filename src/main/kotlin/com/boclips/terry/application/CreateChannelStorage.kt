package com.boclips.terry.application

import com.boclips.terry.infrastructure.outgoing.policy.PolicyRepository
import com.boclips.terry.infrastructure.outgoing.storage.InvalidName
import com.boclips.terry.infrastructure.outgoing.storage.StorageAlreadyExists
import com.boclips.terry.infrastructure.outgoing.storage.StorageCreationFailure
import com.boclips.terry.infrastructure.outgoing.storage.StorageCreationSuccess
import com.boclips.terry.infrastructure.outgoing.storage.StorageRepository
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
                        policyRepository.createOrGet(storageName)?.let { policyId ->
                            userRepository.addPolicyToUser(username, policyId)
                            ChannelCreationSuccess(
                                storageName = storageName,
                                userName = username,
                                policyName = policyId
                            )
                        } ?: ChannelCreationFailed
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
