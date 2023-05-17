package com.boclips.terry.infrastructure.outgoing.sentry

import com.boclips.terry.application.SentryReportParams
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.IOException

@Component
@Profile("test")
@Primary
class FakeSentryClient : SentryClient {

    private val issuesByProject = mutableMapOf<SentryProject, List<SentryProjectIssue>>()

    override fun getProjects(params: SentryReportParams): List<SentryProject> {
        return issuesByProject.keys.toList()
    }

    override fun getProjectIssues(project: SentryProject, params: SentryReportParams): List<SentryProjectIssue> {
        return issuesByProject[project]
            ?.sortedByDescending { it.count }
            ?.take(params.issuesCount)
            ?: throw IOException("Sentry API call failed!")
    }

    fun addProjectWithIssues(project: SentryProject, issues: List<SentryProjectIssue>) {
        issuesByProject[project] = issues
    }

    fun clear() {
        issuesByProject.clear()
    }
}
