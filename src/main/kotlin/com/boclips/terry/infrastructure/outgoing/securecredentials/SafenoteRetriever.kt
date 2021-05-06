package com.boclips.terry.infrastructure.outgoing.securecredentials

import com.boclips.terry.infrastructure.outgoing.rawcredentials.Credential
import com.boclips.terry.infrastructure.outgoing.rawcredentials.CredentialNotFound
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import com.boclips.terry.infrastructure.outgoing.rawcredentials.Retriever as RawRetriever

class SafenoteRetriever(val rawRetriever : RawRetriever) : Retriever {
    override fun get(channelName: String): Response =
        when (val rawCredential = rawRetriever.get(channelName)) {
            is Credential -> {
                val client : WebClient = WebClient.builder()
                    .baseUrl("https://safenote.co")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    .build()

                val uriSpec = client.post()
                val bodySpec = uriSpec.uri("/api/note")
                val headersSpec = bodySpec.bodyValue("Username: ${rawCredential.id}\nPassword: ${rawCredential.secret}")

                val response: WebClient.ResponseSpec = headersSpec.retrieve()

                val entity = response.toBodilessEntity()
                entity.

                SecureCredential()
            }
            CredentialNotFound -> TODO()
        }
}