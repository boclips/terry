package com.boclips.terry.infrastructure.outgoing.securecredentials

import com.boclips.terry.infrastructure.outgoing.rawcredentials.RawCredential
import com.boclips.terry.infrastructure.outgoing.rawcredentials.RawCredentialNotFound
import com.boclips.terry.infrastructure.outgoing.rawcredentials.RawCredentialRetriever
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class SafenoteRetriever(val rawRetriever: RawCredentialRetriever, val url: String = "https://safenote.co/api/note") : SecureCredentialRetriever {
    override fun get(channelName: String): SecureCredentialResponse =
        when (val rawCredential = rawRetriever.get(channelName)) {
            is RawCredential -> {
                val note = "Username: ${rawCredential.id}\nPassword: ${rawCredential.secret}"
                val values = mapOf("note" to note, "lifetime" to 336, "read_count" to 5)
                val mapper = jacksonObjectMapper()
                val json = HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(values))
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    .POST(json)
                    .build()
                val response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString())
                val resp: SecureCredentialResponse = try {
                    val safenote = mapper.readValue(response.body(), Safenote::class.java)
                    SecureCredential(url = safenote.link)
                } catch (e: Exception) {
                    SafenoteFailure(message = e.message ?: "No message")
                }
                resp
            }
            RawCredentialNotFound -> CredentialNotFound
        }
}
