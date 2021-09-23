package com.boclips.terry.infrastructure.outgoing.channels

sealed class ChannelCreationResponse

object ChannelCreationSuccess : ChannelCreationResponse()
object InvalidName : ChannelCreationResponse()

