package com.boclips.terry.infrastructure.outgoing.storage

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.BucketNotificationConfiguration
import com.amazonaws.services.s3.model.S3Event
import com.amazonaws.services.s3.model.TopicConfiguration
import com.boclips.terry.config.AWSNotificationProperties
import java.util.*

class AWSNotificationService(
    private val s3Client: AmazonS3,
    private val awsNotificationProperties: AWSNotificationProperties
) : NotificationService {
    override fun addBucketNotification(bucketName: String) {
        val notificationConfiguration = BucketNotificationConfiguration().addConfiguration(
                "snsTopicConfig",
                TopicConfiguration(
                    awsNotificationProperties.channelTopicArn,
                    EnumSet.of(S3Event.ObjectCreated)
                )
            )

        s3Client.setBucketNotificationConfiguration(bucketName, notificationConfiguration)
    }
}
