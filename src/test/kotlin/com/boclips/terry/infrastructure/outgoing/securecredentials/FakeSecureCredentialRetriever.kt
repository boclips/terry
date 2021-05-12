package com.boclips.terry.infrastructure.outgoing.securecredentials

import com.boclips.terry.Fake

class FakeSecureCredentialRetriever : Fake, SecureCredentialRetriever {
    var nextResponse: SecureCredentialResponse? = null

    override fun reset(): Fake =
        this
            .also { nextResponse = null }

    override fun get(channelName: String): SecureCredentialResponse =
        nextResponse!!

    fun respondWith(response: SecureCredentialResponse): FakeSecureCredentialRetriever =
        this
            .also { nextResponse = response }
}