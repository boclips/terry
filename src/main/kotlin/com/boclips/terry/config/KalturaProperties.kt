package com.boclips.terry.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("kaltura")
class KalturaProperties {
    lateinit var partnerId: String
    lateinit var userId: String
    lateinit var secret: String
}