package com.boclips.terry.infrastructure.outgoing.rawcredentials

sealed class Response

data class Credential(
    val id: String,
    val secret: String
) : Response()

object CredentialNotFound : Response()
