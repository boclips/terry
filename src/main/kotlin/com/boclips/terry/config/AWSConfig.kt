package com.boclips.terry.config

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.boclips.terry.infrastructure.outgoing.policy.IamPolicyRepository
import com.boclips.terry.infrastructure.outgoing.policy.PolicyRepository
import com.boclips.terry.infrastructure.outgoing.storage.AWSNotificationService
import com.boclips.terry.infrastructure.outgoing.storage.AWSStorageRepository
import com.boclips.terry.infrastructure.outgoing.storage.NotificationService
import com.boclips.terry.infrastructure.outgoing.storage.StorageRepository
import com.boclips.terry.infrastructure.outgoing.users.IamUserRepository
import com.boclips.terry.infrastructure.outgoing.users.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
class AWSConfig(
    private val awsNotificationProperties: AWSNotificationProperties
) {
    @Bean
    fun s3Client(): AmazonS3 = AmazonS3ClientBuilder
        .standard()
        .withRegion(Regions.EU_WEST_1)
        .withCredentials(EnvironmentVariableCredentialsProvider())
        .build()

    @Bean
    fun channelRepository(s3Client: AmazonS3, notificationService: NotificationService): StorageRepository =
        AWSStorageRepository(s3Client, notificationService)

    @Bean
    fun policyRepository(): PolicyRepository = IamPolicyRepository()

    @Bean
    fun userRepository(): UserRepository = IamUserRepository()

    @Bean
    fun notificationService(s3Client: AmazonS3): NotificationService = AWSNotificationService(s3Client, awsNotificationProperties)
}
