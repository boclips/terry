package com.boclips.terry.infrastructure.outgoing.storage

import com.amazonaws.services.s3.AmazonS3
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test

class AWSStorageRepositoryTest {

    @Test
    fun `Sets up a notification policy when a bucket is created`() {
        val mockS3 = mock<AmazonS3>()
        val mockNotificationService = mock<NotificationService>()
        val storageRepository = AWSStorageRepository(mockS3, mockNotificationService)

        whenever(mockS3.createBucket("my-new-bucket")).thenReturn(any())

        storageRepository.create("my-new-bucket")

        verify(mockNotificationService, times(1)).addBucketNotification("my-new-bucket")
    }
}
