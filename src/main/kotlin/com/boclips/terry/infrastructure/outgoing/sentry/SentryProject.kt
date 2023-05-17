package com.boclips.terry.infrastructure.outgoing.sentry

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SentryProject(
    var id: String? = null,
    var slug: String? = null
)
