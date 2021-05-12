package com.boclips.terry.infrastructure.outgoing.securecredentials

interface SecureCredentialRetriever {
    fun get(channelName: String): SecureCredentialResponse
}
