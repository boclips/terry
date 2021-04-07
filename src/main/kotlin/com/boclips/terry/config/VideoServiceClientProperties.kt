package com.boclips.terry.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Profile("!test")
@Component
@Validated
@ConfigurationProperties(prefix = "video-service")
class VideoServiceClientProperties {
    @NotBlank
    var baseUrl: String = ""

    @NotBlank
    var clientId: String = ""

    @NotBlank
    var clientSecret: String = ""
}
