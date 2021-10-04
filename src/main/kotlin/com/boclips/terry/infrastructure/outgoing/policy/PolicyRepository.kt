package com.boclips.terry.infrastructure.outgoing.policy

interface PolicyRepository {
    fun create(storageName: String): String?
    fun delete(policyId: String): Boolean
}
