package com.boclips.terry.infrastructure.outgoing.sentry

import com.boclips.terry.application.SentryReportParams

interface SentryClient {

    fun getProjects(params: SentryReportParams): List<SentryProject>
    fun getProjectIssues(project: SentryProject, params: SentryReportParams): List<SentryProjectIssue>
}
