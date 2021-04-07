package com.boclips.terry.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("slack")
class SlackProperties {
    lateinit var signingSecret: String
    lateinit var botToken: String
}