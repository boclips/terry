package com.boclips.terry.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.terry.application.CreateChannelStorage
import com.boclips.terry.infrastructure.Clock
import com.boclips.terry.infrastructure.FakeClock
import com.boclips.terry.infrastructure.outgoing.policy.FakePolicyRepository
import com.boclips.terry.infrastructure.outgoing.slack.FakeSlackPoster
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import com.boclips.terry.infrastructure.outgoing.storage.FakeStorageRepository
import com.boclips.terry.infrastructure.outgoing.users.FakeUserRepository
import com.boclips.terry.infrastructure.outgoing.videos.FakeVideoService
import com.boclips.terry.infrastructure.outgoing.videos.VideoService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor

@Configuration
@Profile("test")
class TestContext {
    @Bean
    fun createChannel(): CreateChannelStorage = CreateChannelStorage(
        storageRepository = FakeStorageRepository(),
        userRepository = FakeUserRepository(),
        policyRepository = FakePolicyRepository()
    )

    @Bean
    fun fakeSlackPoster(): SlackPoster = FakeSlackPoster()

    @Bean
    @Primary
    fun fakeClock(): Clock = FakeClock()

    @Bean
    @Primary
    fun fakeVideoService(): VideoService = FakeVideoService()

    @Bean
    fun fakeKalturaClient(): KalturaClient = TestKalturaClient()

    @Bean
    @Primary
    fun fakeRawCredentialRetriever(): com.boclips.terry.infrastructure.outgoing.rawcredentials.RawCredentialRetriever =
        com.boclips.terry.infrastructure.outgoing.rawcredentials.FakeRawCredentialRetriever()

    @Bean
    @Primary
    fun fakeSecureCredentialRetriever(): com.boclips.terry.infrastructure.outgoing.securecredentials.SecureCredentialRetriever =
        com.boclips.terry.infrastructure.outgoing.securecredentials.FakeSecureCredentialRetriever()

    @Bean
    @Primary
    fun taskExecutor(): TaskExecutor {
        return SyncTaskExecutor()
    }
}
