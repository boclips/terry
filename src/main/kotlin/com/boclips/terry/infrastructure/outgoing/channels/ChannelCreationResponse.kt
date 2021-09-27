package com.boclips.terry.infrastructure.outgoing.channels

sealed class ChannelCreationResponse

data class ChannelCreationSuccess(val user: String) : ChannelCreationResponse()
object InvalidName : ChannelCreationResponse()

