package com.boclips.terry.infrastructure.outgoing.sentry

import com.boclips.terry.application.SentryReportFailure
import com.boclips.terry.application.SentryReportParams
import com.boclips.terry.application.SentryReportResponse
import com.boclips.terry.application.SentryReportSuccessful
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

        return try {
            val projects = sentryClient.getProjects(params)

            val sentryIssuesForReport = projects.chunked(6)
                .flatMap { chunkOfProjects ->
                    runBlocking(Dispatchers.Default) {
                        chunkOfProjects.map { async { sentryClient.getProjectIssues(it, params) } }
                            .awaitAll()
                    }
                }.flatten()
                .sortedByDescending { it.count }
                .take(params.issuesCount)
                .filter { it.count?.let { count -> count > params.threshold } ?: true }

            logger.info { "sentry report created in ${time.elapsed(MILLISECONDS)}ms" }
            SentryReportSuccessful(sentryIssuesForReport, params)

        } catch (exception: Exception) {
            logger.warn(exception) { "Composing sentry report failed! Creating report failure instead" }
            SentryReportFailure(exception.message)
        }
    }
}
