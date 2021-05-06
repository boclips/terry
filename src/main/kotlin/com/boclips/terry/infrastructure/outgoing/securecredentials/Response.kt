package com.boclips.terry.infrastructure.outgoing.securecredentials

sealed class Response

data class SecureCredential(val url: String) : Response()
object CredentialNotFound : Response()
data class SafenoteFailure(val message: String) : Response()
