package com.boclips.terry.application

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class SentryReportResponseTest {

    @Test
    fun `responds with special message when no items to report`() {
        val params = SentryReportParams()
        val report = SentryReportSuccessful(emptyList(), params).generate()

        Assertions.assertThat(report).isEqualTo("""
        |🚨 *Sizzling Sentry report - [last ${params.period} / ${params.team} / ${params.environment} / threshold ${params.threshold}]* 🚨 
        |
        |Looks like there have been no issues for given parameters! 🥹
        """.trimMargin())
    }
}