package com.boclips.terry.infrastructure.outgoing.users

interface UserRepository {
    fun create(user: String): UserCreationResponse
}