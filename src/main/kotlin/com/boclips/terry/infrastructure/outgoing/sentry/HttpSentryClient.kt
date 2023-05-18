package com.boclips.terry.infrastructure.outgoing.sentry

import com.boclips.terry.application.SentryReportParams
import com.boclips.terry.config.SentryProperties
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class HttpSentryClient(private val sentryProperties: SentryProperties) : SentryClient {

    private val httpClient: OkHttpClient = OkHttpClient()

    companion object : KLogging()
    override fun getProjects(params: SentryReportParams): List<SentryProject> {
        val request = Request.Builder()
            .url(projectsUrl(params))
            .get()
            .addHeader("Authorization", "Bearer " + sentryProperties.token)
            .build()

        return httpClient.newCall(request).execute().let { response ->
            if (!response.isSuccessful) throw IOException("Sentry API call failed!")

            val mapper = ObjectMapper()
            return@let mapper.readValue<List<SentryProject>>(
                response.body!!.string(),
                mapper.typeFactory.constructCollectionType(List::class.java, SentryProject::class.java)
            )
        }
    }

    private fun projectsUrl(params: SentryReportParams): String {
        return "https://boclips.sentry.io/api/0/teams/boclips/${params.team}/projects/"
    }

    override fun getProjectIssues(project: SentryProject, params: SentryReportParams): List<SentryProjectIssue> {
        logger.info { "Getting issues for ${project.slug}" }
        val request = Request.Builder()
            .url(projectIssuesUrl(project, params))
            .get()
            .addHeader("Authorization", "Bearer " + sentryProperties.token)
            .build()

        return OkHttpClient().newCall(request).execute().let { response ->
            if (!response.isSuccessful) throw IOException("Sentry API call failed!")

            val mapper = ObjectMapper()
            return@let mapper.readValue<List<SentryProjectIssue>>(
                response.body!!.string(),
                mapper.typeFactory.constructCollectionType(List::class.java, SentryProjectIssue::class.java)
            )
        }
    }

    private fun projectIssuesUrl(project: SentryProject, params: SentryReportParams): String {
        return "https://boclips.sentry.io/api/0/organizations/boclips/issues/?environment=${params.environment}&project=${project.id}&query=&sort=freq&statsPeriod=${params.period}&limit=${params.issuesCount}&query=is:unresolved"
    }
}
