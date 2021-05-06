package com.boclips.terry.infrastructure.outgoing.credentials

interface CredentialRetriever {
    fun get(channelName: String): ChannelCredentialResponse
}
