package com.boclips.terry.config

import com.boclips.videos.api.httpclient.ChannelsClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.httpclient.test.fakes.ChannelsClientFake
import com.boclips.videos.api.httpclient.test.fakes.VideosClientFake
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
class VideoClientConfigFake {
    @Bean
    fun channelsClient(): ChannelsClient {
        return ChannelsClientFake()
    }

    @Bean
    fun videosClient(): VideosClient {
        return VideosClientFake()
    }
}
