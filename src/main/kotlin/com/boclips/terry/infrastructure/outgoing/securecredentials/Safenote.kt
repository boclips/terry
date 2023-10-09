package com.boclips.terry.infrastructure.outgoing.securecredentials

data class Safenote(
    val success: Boolean,
    val key: String,
    val link: String
)
