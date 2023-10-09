package com.boclips.terry.infrastructure.outgoing.storage

interface StorageRepository {
    fun create(name: String): StorageCreationResponse
    fun delete(name: String): StorageDeletionResponse
}
