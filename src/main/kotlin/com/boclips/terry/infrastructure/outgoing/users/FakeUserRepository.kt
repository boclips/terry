package com.boclips.terry.infrastructure.outgoing.users

class FakeUserRepository : UserRepository {
    val users = mutableListOf<String>()
    override fun create(user: String): UserCreationResponse {
        users.add(user)
        return UserCreated(username = user, userId = "AIDAY$user")
    }

    fun exists(user: String) = users.contains(user)
}