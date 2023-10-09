package com.boclips.terry.infrastructure.outgoing.storage

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.IllegalBucketNameException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test

class AWSStorageRepositoryTest {
    private val mockS3 = mock<AmazonS3>()
    private val mockNotificationService = mock<NotificationService>()
    private val storageRepository = AWSStorageRepository(mockS3, mockNotificationService)

    @Test
    fun `Sets up a notification policy when a bucket is created`() {
        whenever(mockS3.createBucket("boclips-upload-my-new-bucket")).thenReturn(any())

        storageRepository.create("my-new-bucket")

        verify(mockNotificationService, times(1)).addBucketNotification("boclips-upload-my-new-bucket")
    }

    @Test
    fun `does not set up notification when bucket creation fails`() {
        whenever(mockS3.createBucket("boclips-upload-my-new-bucket"))
            .thenThrow(
                IllegalBucketNameException("bad bucket")
            )

        storageRepository.create("my-new-bucket")

        verify(mockNotificationService, never()).addBucketNotification(any())
    }
}
