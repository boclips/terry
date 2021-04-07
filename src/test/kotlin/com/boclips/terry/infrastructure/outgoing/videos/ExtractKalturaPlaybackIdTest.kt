package com.boclips.terry.infrastructure.outgoing.videos

import io.kotlintest.properties.assertAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExtractKalturaPlaybackIdTest {
    @Test
    fun `parses URIs that contain the unique Kaltura ID`() {
        assertThat(
            extractKalturaPlaybackId(
                "https://cdnapisec.kaltura.com/p/1776261/sp/177626100/playManifest/entryId/1_y0g6ftvy/format/applehttp/protocol/https/video.mp4"
            )
        ).isEqualTo("1_y0g6ftvy")
    }

    @Test
    fun `is null for arbitrary input`() {
        assertAll { garbage: String ->
            assertThat(extractKalturaPlaybackId(garbage)).isNull()
        }
    }
}
