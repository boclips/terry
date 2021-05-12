package com.boclips.terry.infrastructure.outgoing.rawcredentials

sealed class RawCredentialResponse

data class RawCredential(
    val id: String,
    val secret: String
) : RawCredentialResponse()

object RawCredentialNotFound : RawCredentialResponse()
