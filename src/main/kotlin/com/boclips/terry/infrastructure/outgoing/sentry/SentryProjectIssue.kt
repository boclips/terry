package com.boclips.terry.infrastructure.outgoing.sentry

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@JsonIgnoreProperties(ignoreUnknown = true)
data class SentryProjectIssue(
    val project: SentryProject? = null,
    val metadata: SentryIssueMetadata? = null,
    val count: Int? = null,
    val culprit: String? = null,
    val permalink: String? = null,
    @JsonIgnore
    var firstSeen: LocalDateTime? = null
) {

    @JsonProperty("lifetime")
    fun extractFirstSeen(lifetime: Map<String, String>) {
        firstSeen = try {
            LocalDateTime.parse(lifetime["firstSeen"], DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"))}
        catch (ex: Exception) {
            null
        }
    }

    fun isFirstSeenDuringLastDay(): Boolean {
        return firstSeen?.isAfter(LocalDateTime.now().minusDays(1)) ?: false
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class SentryIssueMetadata(
    val type: String? = null,
    val value: String? = null
)
