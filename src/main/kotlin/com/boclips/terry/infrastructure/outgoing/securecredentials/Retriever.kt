package com.boclips.terry.infrastructure.outgoing.securecredentials

interface Retriever {
    fun get(channelName: String): Response
}
