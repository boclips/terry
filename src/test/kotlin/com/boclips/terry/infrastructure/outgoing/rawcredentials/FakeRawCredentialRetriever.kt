package com.boclips.terry.infrastructure.outgoing.rawcredentials

import com.boclips.terry.Fake

class FakeRawCredentialRetriever : Fake, RawCredentialRetriever {
    var lastChannelNameReceived: String? = null
    private var nextResponse: RawCredentialResponse? = null

    override fun get(channelName: String): RawCredentialResponse =
        nextResponse!!
            .also { lastChannelNameReceived = channelName }

    override fun reset(): Fake =
        this
            .also {
                nextResponse = null
                lastChannelNameReceived = null
            }

    fun respondWith(response: RawCredentialResponse): FakeRawCredentialRetriever = this
        .also { nextResponse = response }
}
