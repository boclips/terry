package com.boclips.terry.infrastructure.outgoing.storage

sealed class StorageDeletionResponse

object StorageDeletionSuccess : StorageDeletionResponse()
object StorageDeletionFailed : StorageDeletionResponse()
