package com.boclips.terry.infrastructure.outgoing.safenote

sealed class Response

data class Safenote(val url: String) : Response()
data class SafenoteFailure(val message: String) : Response()
