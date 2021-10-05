package com.boclips.terry.infrastructure.outgoing.users

interface UserRepository {
    fun create(user: String): UserCreationResponse
    fun addPolicyToUser(userName: String, policyId: String): Boolean
    fun removePolicyFromUser(userName: String, policyId: String): Boolean
}
