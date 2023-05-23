package com.boclips.terry.infrastructure.outgoing

import com.boclips.terry.application.SentryReportFailure
import com.boclips.terry.application.SentryReportParams
import com.boclips.terry.config.SentryProperties
import com.boclips.terry.infrastructure.outgoing.sentry.ComposeSentryReport
import com.boclips.terry.infrastructure.outgoing.sentry.FakeSentryClient
import com.boclips.terry.infrastructure.outgoing.sentry.HttpSentryClient
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.SentryProjectFactory
import testsupport.SentryProjectIssueFactory
import java.time.LocalDateTime

class ComposeSentryReportTest {
    @Test
    fun `it works!`() {
        ComposeSentryReport(HttpSentryClient(SentryProperties("c789f711181a4f17937eeff56ba4d91edfe42a3052f54ddd9cb40b744cb4841f"))).invoke(SentryReportParams())
    }

    @Test
    fun `returns failure when sentryClient throwing an exception`() {
        val sentryClient = FakeSentryClient()

        sentryClient.throwExceptionWhenInteracting("something went wrong")
        val project1 = SentryProjectFactory.sample("1", "service-1")

        sentryClient.addProjectWithIssues(
            project = project1,
            listOf(
                SentryProjectIssueFactory.sample(
                    project = project1,
                    count = 4,
                    type = "NPE",
                    title = "This is NPE",
                    firstSeen = LocalDateTime.now().minusHours(1)
                )
            )
        )
        val reportResponse = ComposeSentryReport(sentryClient).invoke(SentryReportParams())

        Assertions.assertThat(reportResponse).isInstanceOf(SentryReportFailure::class.java)
        Assertions.assertThat(reportResponse.generate()).contains("something went wrong")
    }
}
