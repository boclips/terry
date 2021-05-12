package com.boclips.terry.infrastructure.outgoing.securecredentials

sealed class SecureCredentialResponse

data class SecureCredential(val url: String) : SecureCredentialResponse()
object CredentialNotFound : SecureCredentialResponse()
data class SafenoteFailure(val message: String) : SecureCredentialResponse()
