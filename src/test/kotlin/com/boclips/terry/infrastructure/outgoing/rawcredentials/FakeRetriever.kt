package com.boclips.terry.infrastructure.outgoing.rawcredentials

import com.boclips.terry.Fake

class FakeRetriever : Fake, Retriever {
    var lastChannelNameReceived: String? = null
    private var nextResponse: Response? = null

    override fun get(channelName: String): Response =
        nextResponse!!
            .also { lastChannelNameReceived = channelName }

    override fun reset(): Fake =
        this
            .also {
                nextResponse = null
                lastChannelNameReceived = null
            }

    fun respondWith(response: Response): FakeRetriever = this
        .also { nextResponse = response }
}
