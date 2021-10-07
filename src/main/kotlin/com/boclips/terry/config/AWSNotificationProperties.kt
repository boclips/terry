package com.boclips.terry.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Configuration
@ConfigurationProperties("aws-notifications")
@Validated
class AWSNotificationProperties {
    @NotBlank
    lateinit var channelTopicArn: String
}
