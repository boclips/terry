package com.boclips.terry.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.KalturaClientConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KalturaConfig {

    @Bean
    @ConditionalOnMissingBean(KalturaClient::class)
    fun kalturaClient(kalturaProperties: KalturaProperties): KalturaClient = KalturaClient.create(
        KalturaClientConfig.builder()
            .partnerId(kalturaProperties.partnerId)
            .userId(kalturaProperties.userId)
            .secret(kalturaProperties.secret)
            .build()
    )
}