package com.boclips.terry.application

sealed class ChannelDeletionResponse

object ChannelDeletionSuccess : ChannelDeletionResponse()
object ChannelDeletionFailed : ChannelDeletionResponse()

