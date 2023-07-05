package com.boclips.terry.application

import com.boclips.terry.infrastructure.outgoing.sentry.SentryProjectIssue
import java.util.StringJoiner

sealed class SentryReportResponse {
    abstract fun generate(): String
}

data class SentryReportSuccessful(val sentryIssues: List<SentryProjectIssue>, val params: SentryReportParams) : SentryReportResponse() {
    override fun generate(): String {
        if(sentryIssues.isEmpty()) {
            return generateReportWithNoIssues()
        }

        return """
        |🚨 *Sizzling Sentry report - [last ${params.period} / ${params.team} / ${params.environment} / threshold ${params.threshold}]* 🚨 
        |
        |Top ${params.issuesCount} unresolved issues: 
        |
        |${generateIssuesSection()}
        """.trimMargin()
    }

    private fun generateReportWithNoIssues() = """
            |🚨 *Sizzling Sentry report - [last ${params.period} / ${params.team} / ${params.environment} / threshold ${params.threshold}]* 🚨 
            |
            |Looks like there have been no issues for given parameters! 🥹
            """.trimMargin()

    private fun generateIssuesSection(): String {
        return sentryIssues
            .joinToString(separator = System.lineSeparator().repeat(3)) { turnIssueIntoReportItem(it) }
    }

    private fun turnIssueIntoReportItem(issue: SentryProjectIssue): String {
        val reportBuilder = StringJoiner(System.lineSeparator())
            .add("""👉 *[${issue.count}x] [${issue.project?.slug ?: "N/A"}] - ${issue.metadata?.type ?: "N/A"}* (<${issue.permalink}|details>)""")

        if (issue.isFirstSeenDuringLastDay()) {
            reportBuilder.add("""    🐛 *first appearance in the last 24hrs*""")
        }

        if (issue.metadata?.value != null && issue.metadata.value.trim().length > 2) {
            reportBuilder.add("""       • _${issue.metadata.value}_""")
        }

        if (issue.culprit != null && issue.culprit.trim().length > 2) {
            reportBuilder.add("""       • _${issue.culprit}_""")
        }

        return reportBuilder.toString()
    }
}

data class SentryReportFailure(val failureMessage: String?) : SentryReportResponse() {
    override fun generate(): String {
        return """
        |I am terribly sorry but something stopped me from generating that report!
        |The reason of my failure is: $failureMessage
    """.trimMargin()
    }
}
