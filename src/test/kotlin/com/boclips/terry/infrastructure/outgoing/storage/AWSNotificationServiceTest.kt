package com.boclips.terry.infrastructure.outgoing.storage

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.BucketNotificationConfiguration
import com.amazonaws.services.s3.model.S3Event
import com.amazonaws.services.s3.model.TopicConfiguration
import com.boclips.terry.config.AWSNotificationProperties
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AWSNotificationServiceTest {

    @Test
    fun `calls aws notification setup for requested bucket`() {
        val fakeS3 = mock<AmazonS3>()
        val notificationService =
            AWSNotificationService(
                s3Client = fakeS3,
                awsNotificationProperties = AWSNotificationProperties().apply { this.channelTopicArn = "fake-arn" })

        notificationService.addBucketNotification("our-test-bucket")
        val notificationConfiguration = argumentCaptor<BucketNotificationConfiguration>()

        verify(fakeS3).setBucketNotificationConfiguration(eq("our-test-bucket"), notificationConfiguration.capture())
        val topicConfiguration = notificationConfiguration.firstValue.getConfigurationByName("snsTopicConfig") as TopicConfiguration

        assertThat(topicConfiguration.events).isEqualTo(setOf(S3Event.ObjectCreated.toString()))
        assertThat(topicConfiguration.topicARN).isEqualTo("fake-arn")
    }
}
