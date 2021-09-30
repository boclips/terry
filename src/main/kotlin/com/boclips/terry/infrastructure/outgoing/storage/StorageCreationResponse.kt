package com.boclips.terry.infrastructure.outgoing.storage

sealed class StorageCreationResponse

data class StorageCreationSuccess(val name: String) : StorageCreationResponse()
object InvalidName : StorageCreationResponse()
