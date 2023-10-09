package com.boclips.terry.infrastructure.outgoing.securecredentials

import com.boclips.terry.infrastructure.outgoing.rawcredentials.FakeRawCredentialRetriever
import com.boclips.terry.infrastructure.outgoing.rawcredentials.RawCredential
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class SafenoteRetrieverTest {
    @Test
    fun `obtains credential and wraps it in a Safenote`() {
        val rawRetriever = FakeRawCredentialRetriever()
        rawRetriever.respondWith(RawCredential(id = "AKIAHEREITIS", secret = "somesecret"))
        val retriever = SafenoteRetriever(rawRetriever)
        when (val response = retriever.get("my-channel")) {
            CredentialNotFound -> fail("this test had a stubbed credential - how did this happen?")
            is SecureCredential -> assertThat(response.url).matches("^https://safenote\\.co.*")
        }
    }

    @Test
    @Disabled
    fun `responds appropriately to Safenote failure`() {
        val rawRetriever = FakeRawCredentialRetriever()
        rawRetriever.respondWith(RawCredential(id = "AKIAHEREITIS", secret = "somesecret"))
        val retriever = SafenoteRetriever(rawRetriever, url = "https://httpbin.org/status/503")
        val response = retriever.get("my-channel") as SafenoteFailure
        assertThat(response.message).contains("No content to map due to end-of-input")
    }

    @Test
    fun `responds appropriately to missing credential`() {
        val rawRetriever = FakeRawCredentialRetriever()
        rawRetriever.respondWith(com.boclips.terry.infrastructure.outgoing.rawcredentials.RawCredentialNotFound)
        val retriever = SafenoteRetriever(rawRetriever)
        assertThat(retriever.get("my-channel")).isEqualTo(CredentialNotFound)
    }
}
