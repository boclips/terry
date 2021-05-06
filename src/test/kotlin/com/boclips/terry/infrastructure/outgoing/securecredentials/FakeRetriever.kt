package com.boclips.terry.infrastructure.outgoing.securecredentials

import com.boclips.terry.Fake

class FakeRetriever : Fake, Retriever {
    var nextResponse: Response? = null

    override fun reset(): Fake =
        this
            .also { nextResponse = null }

    override fun get(channelName: String): Response =
        nextResponse!!

    fun respondWith(response: Response): FakeRetriever =
        this
            .also { nextResponse = response }
}