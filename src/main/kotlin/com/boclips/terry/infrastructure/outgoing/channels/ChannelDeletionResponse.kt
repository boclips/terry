package com.boclips.terry.infrastructure.outgoing.channels

sealed class ChannelDeletionResponse

object ChannelDeletionSuccess : ChannelDeletionResponse()

