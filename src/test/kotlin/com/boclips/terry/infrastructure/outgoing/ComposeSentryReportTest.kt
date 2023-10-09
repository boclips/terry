package com.boclips.terry.infrastructure.outgoing

import com.boclips.terry.application.SentryReportFailure
import com.boclips.terry.application.SentryReportParams
import com.boclips.terry.application.SentryReportSuccessful
import com.boclips.terry.infrastructure.outgoing.sentry.ComposeSentryReport
import com.boclips.terry.infrastructure.outgoing.sentry.FakeSentryClient
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testsupport.SentryProjectFactory
import testsupport.SentryProjectIssueFactory
import java.time.LocalDateTime

class ComposeSentryReportTest {
    private val sentryClient = FakeSentryClient()

    @BeforeEach
    fun setUp() {
        sentryClient.clear()
    }

    @Test
    fun `will filter out items that have count below or equal threshold`() {
        val project1 = SentryProjectFactory.sample("1", "service-1")

        sentryClient.addProjectWithIssues(
            project = project1,
            listOf(
                SentryProjectIssueFactory.sample(project = project1, count = 2),
                SentryProjectIssueFactory.sample(project = project1, count = 1),
                SentryProjectIssueFactory.sample(project = project1, count = 0)
            )
        )
        val reportResponse = ComposeSentryReport(sentryClient).invoke(SentryReportParams(threshold = 1))

        Assertions.assertThat((reportResponse as SentryReportSuccessful).sentryIssues).hasSize(1)
        Assertions.assertThat((reportResponse).sentryIssues.first().count).isEqualTo(2)
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
