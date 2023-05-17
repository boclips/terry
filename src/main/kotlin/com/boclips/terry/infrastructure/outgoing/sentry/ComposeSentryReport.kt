package com.boclips.terry.infrastructure.outgoing.sentry

import com.boclips.terry.application.SentryReportParams
import com.boclips.terry.application.SentryReportResponse
import com.google.common.base.Stopwatch
import mu.KLogging
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit.MILLISECONDS

@Component
class ComposeSentryReport(private val sentryClient: SentryClient) {

    companion object : KLogging()

    operator fun invoke(params: SentryReportParams): SentryReportResponse {
        val time = Stopwatch.createStarted()

        val projects = sentryClient.getProjects(params)

        val report =
            projects
                .flatMap { sentryClient.getProjectIssues(it, params) }
                .sortedByDescending { it.count }
                .take(params.issuesCount)
                .joinToString(separator = System.lineSeparator()) { "${it.project!!.slug} - ${it.count} - ${it.metadata!!.type} - ${it.metadata.value}" }


        logger.info { "sentry report created in ${time.elapsed(MILLISECONDS)}ms" }

        return SentryReportResponse(report)
    }
}
