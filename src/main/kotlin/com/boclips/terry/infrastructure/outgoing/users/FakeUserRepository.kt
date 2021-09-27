package com.boclips.terry.infrastructure.outgoing.users

class FakeUserRepository : UserRepository {
    override fun create(user: String): UserCreationResponse {
        return UserCreated(username = user, userId = "AIDAY$user")
    }
}