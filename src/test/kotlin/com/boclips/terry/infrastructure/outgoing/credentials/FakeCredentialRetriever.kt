package com.boclips.terry.infrastructure.outgoing.credentials

import com.boclips.terry.Fake
import com.boclips.terry.infrastructure.outgoing.videos.VideoServiceResponse

class FakeCredentialRetriever : Fake, CredentialRetriever {
    var lastChannelNameReceived: String? = null
    var nextResponse: ChannelCredentialResponse? = null

    override fun get(channelName: String): ChannelCredentialResponse =
        nextResponse!!
            .also { lastChannelNameReceived = channelName }

    override fun reset(): Fake =
        this
            .also {
                nextResponse = null
                lastChannelNameReceived = null
            }

    fun respondWith(response: ChannelCredentialResponse): FakeCredentialRetriever = this
        .also { nextResponse = response }
}
