package com.boclips.terry.infrastructure.outgoing.rawcredentials

interface RawCredentialRetriever {
    fun get(channelName: String): RawCredentialResponse
}
