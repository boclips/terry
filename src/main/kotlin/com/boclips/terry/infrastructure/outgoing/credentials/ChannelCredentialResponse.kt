package com.boclips.terry.infrastructure.outgoing.credentials

sealed class ChannelCredentialResponse

data class CredentialLink(
    val url: String
) : ChannelCredentialResponse()

object CredentialNotFound : ChannelCredentialResponse()
