package com.boclips.terry.config

import com.boclips.terry.infrastructure.outgoing.channels.AWSChannelRepository
import com.boclips.terry.infrastructure.outgoing.channels.ChannelRepository
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
class ChannelConfig {
    fun channelRepository() : ChannelRepository =
        AWSChannelRepository()
}
