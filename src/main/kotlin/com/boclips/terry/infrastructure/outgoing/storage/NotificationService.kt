package com.boclips.terry.infrastructure.outgoing.storage

interface NotificationService {
    fun addBucketNotification(bucketName: String)
}
