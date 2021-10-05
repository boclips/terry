package com.boclips.terry.infrastructure.outgoing.users

sealed class UserCreationResponse
data class UserCreated(val username: String, val userId: String): UserCreationResponse()