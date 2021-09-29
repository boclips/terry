package com.boclips.terry.application

sealed class ChannelCreationResponse

data class ChannelCreationSuccess(val storageName: String) : ChannelCreationResponse()
object InvalidName : ChannelCreationResponse()

