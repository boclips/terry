package com.boclips.terry.infrastructure.outgoing

import com.boclips.terry.application.SentryReportParams
import com.boclips.terry.infrastructure.outgoing.sentry.ComposeSentryReport
import com.boclips.terry.infrastructure.outgoing.sentry.FakeSentryClient
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ComposeSentryReportTest {
    @Test
    @Disabled
    fun `it works!`() {
        ComposeSentryReport(FakeSentryClient()).invoke(SentryReportParams())
    }
}
