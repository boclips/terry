package com.boclips.terry.infrastructure.outgoing.sentry

import com.boclips.terry.application.SentryReportParams
import com.boclips.terry.application.SentryReportResponse
import com.google.common.base.Stopwatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit.MILLISECONDS

@Component
class ComposeSentryReport(private val sentryClient: SentryClient) {
    companion object : KLogging()

    operator fun invoke(params: SentryReportParams): SentryReportResponse {
        val time = Stopwatch.createStarted()

        val projects = sentryClient.getProjects(params)

        val report = runBlocking(Dispatchers.Default) {
            projects
                .map { async { sentryClient.getProjectIssues(it, params) } }
                .awaitAll()
                .flatten()
                .sortedByDescending { it.count }
                .take(params.issuesCount)
                .map { issueReport(it) }
                .joinToString(separator = System.lineSeparator().repeat(3))
        }

        logger.info { "sentry report created in ${time.elapsed(MILLISECONDS)}ms" }

        return SentryReportResponse(report)
    }

    private fun issueReport(issue: SentryProjectIssue): String {
        if (issue.metadata!!.value!!.trim().length > 2) {
            return """
        |👉 *[${issue.count}x] [${issue.project!!.slug}] - ${issue.metadata.type}* (<${issue.permalink}|details>)
        |       • _${issue.metadata.value}_
        |       • _${issue.culprit}_
        """.trimMargin()
        } else {
            return """
        |👉 *[${issue.count}x] [${issue.project!!.slug}] - ${issue.metadata.type}* (<${issue.permalink}|details>)
        |       • _${issue.culprit}_
        """.trimMargin()
        }
    }
}
