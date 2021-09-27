package com.boclips.terry.infrastructure.outgoing.channels

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementAsyncClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class ChannelRepositoryTest {
    var channelRepository: ChannelRepository? = null

    @Test
    fun `it returns a successful creation response when successful`() {
        assertThat(channelRepository!!.delete("test-test-test")).
            isEqualTo(ChannelDeletionSuccess)
        assertThat(channelRepository!!.create("test-test-test"))
            .isEqualTo(ChannelCreationSuccess(user = "test-test-test"))
    }

    @Test
    fun `it returns a failure about the user when username is invalid`() {
        assertThat(channelRepository!!.create("cannothave!exclamation"))
            .isEqualTo(InvalidName)
    }
}

class AWSChannelRepositoryTest : ChannelRepositoryTest() {
    @BeforeEach
    internal fun setUp() {
        channelRepository = AWSChannelRepository()
    }

    @Test
    fun `creates user and returns for the bucket`() {
        val channelName = "channel-name"
        val creationResponse = channelRepository!!.create(channelName)

        assertThat((creationResponse as ChannelCreationSuccess).user).isEqualTo(channelName)
    }

    @Test
    fun `the user can access the bucket`() {
        val s3 = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.EU_WEST_1)
            .withCredentials(EnvironmentVariableCredentialsProvider())
            .build()

//        val iam = AmazonIdentityManagementAsyncClientBuilder
//            .standard()
//            .withRegion(Regions.EU_WEST_1)
//            .withCredentials()
    }

    @Test
    fun `the user cannot access other buckets`() {
    }
}

class FakeChannelRepositoryTest : ChannelRepositoryTest() {
    @BeforeEach
    internal fun setUp() {
        channelRepository = FakeChannelRepository()
    }
}
