package com.boclips.terry.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("sentry")
class SentryProperties(
    var token: String? = null
)
