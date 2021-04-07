package com.boclips.terry.config

import com.boclips.videos.api.httpclient.ChannelsClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.httpclient.helper.ServiceAccountCredentials
import com.boclips.videos.api.httpclient.helper.ServiceAccountTokenFactory
import feign.opentracing.TracingClient
import io.opentracing.Tracer
import feign.okhttp.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
class VideoClientConfig {
    @Bean
    fun videosClient(properties: VideoServiceClientProperties, tracer: Tracer): VideosClient {
        return VideosClient.create(
            apiUrl = properties.baseUrl,
            tokenFactory = ServiceAccountTokenFactory(
                ServiceAccountCredentials(
                    authEndpoint = properties.baseUrl,
                    clientId = properties.clientId,
                    clientSecret = properties.clientSecret
                )
            ),
            feignClient = TracingClient(OkHttpClient(), tracer)
        )
    }
}
