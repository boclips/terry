package com.boclips.terry.infrastructure.outgoing.rawcredentials

interface Retriever {
    fun get(channelName: String): Response
}
