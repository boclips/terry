package com.boclips.terry.infrastructure.outgoing.policy

interface PolicyRepository {
    fun createOrGet(storageName: String): String?
    fun delete(policyId: String): Boolean
}
