package com.boclips.terry.infrastructure.outgoing.sentry

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SentryProjectIssue(
    val project: SentryProject? = null,
    val metadata: SentryIssueMetadata? = null,
    val count: Int? = null,
    val culprit: String? = null,
    val permalink: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SentryIssueMetadata(
    val type: String? = null,
    val value: String? = null
)
