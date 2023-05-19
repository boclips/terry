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
import java.lang.StringBuilder
import java.util.StringJoiner
import java.util.concurrent.TimeUnit.MILLISECONDS

@Component
class ComposeSentryReport(private val sentryClient: SentryClient) {
    companion object : KLogging()

    operator fun invoke(params: SentryReportParams): SentryReportResponse {
        val time = Stopwatch.createStarted()

        val projects = sentryClient.getProjects(params)

        val report = projects.chunked(6)
            .flatMap { chunkOfProjects ->
                runBlocking(Dispatchers.Default) {
                    chunkOfProjects.map { async { sentryClient.getProjectIssues(it, params) } }
                        .awaitAll()
                }
            }.flatten()
            .sortedByDescending { it.count }
            .take(params.issuesCount)
            .joinToString(separator = System.lineSeparator().repeat(3)) { issueReport(it) }

        logger.info { "sentry report created in ${time.elapsed(MILLISECONDS)}ms" }

        return SentryReportResponse(report)
    }

    private fun issueReport(issue: SentryProjectIssue): String {
        val reportBuilder = StringJoiner(System.lineSeparator())
            .add("""👉 *[${issue.count}x] [${issue.project!!.slug}] - ${issue.metadata!!.type}* (<${issue.permalink}|details>)""")

        if (issue.isFirstSeenDuringLastDay()) {
            reportBuilder.add("""    🐛 *first appearance in the last 24hrs*""")
        }

        if (issue.metadata.value!!.trim().length > 2) {
            reportBuilder.add("""       • _${issue.metadata.value}_""")
        }

        if (issue.culprit!!.trim().length > 2) {
            reportBuilder.add("""       • _${issue.culprit}_""")
        }

        return reportBuilder.toString()
    }
}
