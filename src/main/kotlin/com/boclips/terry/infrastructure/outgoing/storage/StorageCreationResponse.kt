package com.boclips.terry.infrastructure.outgoing.storage

sealed class StorageCreationResponse

data class StorageCreationSuccess(val name: String) : StorageCreationResponse()
object StorageAlreadyExists : StorageCreationResponse()
object InvalidName : StorageCreationResponse()
data class StorageCreationFailure(val message: String) : StorageCreationResponse()
