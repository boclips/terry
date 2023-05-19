package com.boclips.terry.infrastructure.outgoing.sentry

import com.boclips.terry.application.SentryReportParams
import com.boclips.terry.config.SentryProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import mu.KLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.http.client.utils.URIBuilder
import org.springframework.stereotype.Component
import java.net.URL

@Component
class HttpSentryClient(private val sentryProperties: SentryProperties) : SentryClient {
    companion object : KLogging()

    private val httpClient: OkHttpClient = OkHttpClient()
    private val mapper = ObjectMapper()

    override fun getProjects(params: SentryReportParams): List<SentryProject> {
        val response = httpClient.newCall(buildRequest(projectsUrl(params))).execute()

        verifyResponseSuccessful(response)

        return mapper.readValue(response.body!!.string(), buildListTypeFor(SentryProject::class.java, mapper))

    }

    override fun getProjectIssues(project: SentryProject, params: SentryReportParams): List<SentryProjectIssue> {
        logger.info { "Getting issues for ${project.slug}" }

        val response = httpClient.newCall(buildRequest(projectIssuesUrl(project, params))).execute()

        verifyResponseSuccessful(response)

        return mapper.readValue(response.body!!.string(), buildListTypeFor(SentryProjectIssue::class.java, mapper))
    }

    private fun buildRequest(url: URL): Request {
        return Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer " + sentryProperties.token)
            .build()
    }

    private fun verifyResponseSuccessful(response: Response) {
        if (!response.isSuccessful) {
            throw SentryApiException(response.message)
        }
    }

    private fun buildListTypeFor(type: Class<*>, mapper: ObjectMapper): CollectionType {
        return mapper.typeFactory.constructCollectionType(List::class.java, type)
    }

    private fun projectsUrl(params: SentryReportParams): URL {
        return URIBuilder("https://boclips.sentry.io/api/0/teams/boclips/${params.team}/projects/").build().toURL()
    }

    private fun projectIssuesUrl(project: SentryProject, params: SentryReportParams): URL {
        return URIBuilder("https://boclips.sentry.io/api/0/organizations/boclips/issues/")
            .addParameter("environment", params.environment)
            .addParameter("project", project.id!!)
            .addParameter("query", "is:unresolved")
            .addParameter("sort", "freq")
            .addParameter("statsPeriod", params.period)
            .addParameter("limit", params.issuesCount.toString())
            .build().toURL()
    }
}
