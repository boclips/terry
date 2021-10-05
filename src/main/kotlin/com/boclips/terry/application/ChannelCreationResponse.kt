package com.boclips.terry.application

sealed class ChannelCreationResponse

data class ChannelCreationSuccess(
    val storageName: String,
    val userName: String,
    val policyName: String
) : ChannelCreationResponse()

object InvalidChannelName : ChannelCreationResponse()
object ChannelAlreadyExists : ChannelCreationResponse()
object InvalidPolicyName : ChannelCreationResponse()
object ChannelCreationFailed : ChannelCreationResponse()

