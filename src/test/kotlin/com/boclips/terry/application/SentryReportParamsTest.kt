package com.boclips.terry.application

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class SentryReportParamsTest {

    @Test
    fun `can extract params`() {
        val params =
            SentryReportParams.extractFromText("gimme sentry report for period 7d with issues 20 from team data and environment staging threshold 2")

        Assertions.assertThat(params.team).isEqualTo("data")
        Assertions.assertThat(params.period).isEqualTo("7d")
        Assertions.assertThat(params.issuesCount).isEqualTo(20)
        Assertions.assertThat(params.environment).isEqualTo("staging")
        Assertions.assertThat(params.threshold).isEqualTo(2)
    }
}
