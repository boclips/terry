package com.boclips.terry.infrastructure.outgoing.channels

sealed class ChannelCreationResponse

data class ChannelCreationSuccess(val storageName: String) : ChannelCreationResponse()
object InvalidName : ChannelCreationResponse()

