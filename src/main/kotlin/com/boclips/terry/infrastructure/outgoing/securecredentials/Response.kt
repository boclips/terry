package com.boclips.terry.infrastructure.outgoing.securecredentials

import com.boclips.terry.infrastructure.outgoing.safenote.Response as SafenoteResponse

sealed class Response

data class SecureCredential(val url: String) : Response()
object CredentialNotFound : Response()
data class SafenoteFailure(val safenoteResponse: SafenoteResponse)
