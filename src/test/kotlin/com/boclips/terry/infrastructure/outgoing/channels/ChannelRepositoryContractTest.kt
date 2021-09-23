package com.boclips.terry.infrastructure.outgoing.channels

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
            .isEqualTo(ChannelCreationSuccess)
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
}

class FakeChannelRepositoryTest : ChannelRepositoryTest() {
    @BeforeEach
    internal fun setUp() {
        channelRepository = FakeChannelRepository()
    }
}
