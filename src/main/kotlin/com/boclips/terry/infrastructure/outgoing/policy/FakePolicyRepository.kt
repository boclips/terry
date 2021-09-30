package com.boclips.terry.infrastructure.outgoing.policy

class FakePolicyRepository : PolicyRepository {
    val policies = mutableListOf<String>()

    override fun create(storageName: String): String? {

        return null
    }

    fun existsFor(channelStorageName: String) = policies.contains(channelStorageName)
}
