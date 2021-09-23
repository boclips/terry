package com.boclips.terry.infrastructure.outgoing.channels

class FakeChannelRepository : ChannelRepository {
    override fun create(name: String): ChannelCreationResponse =
        if (name.contains("!")) {
            InvalidName
        } else {
            ChannelCreationSuccess
        }

    override fun delete(name: String): ChannelDeletionResponse =
        ChannelDeletionSuccess
}