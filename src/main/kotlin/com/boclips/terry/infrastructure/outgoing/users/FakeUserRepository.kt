package com.boclips.terry.infrastructure.outgoing.users

class FakeUserRepository : UserRepository {
    val users = mutableMapOf<String, String?>()
    override fun create(user: String): UserCreationResponse {
        users[user] = null
        return UserCreated(username = user, userId = "AIDAY$user")
    }

    override fun addPolicyToUser(userName: String, policyName: String): Boolean {
        if (exists(userName)) {
            users[userName] = policyName
            return true
        }
        return false
    }

    override fun removePolicyFromUser(userName: String, policyId: String): Boolean {
        if (exists(userName)) {
            users.remove(userName)
            return true
        }
        return false
    }

    fun exists(user: String) = users.contains(user)
}
