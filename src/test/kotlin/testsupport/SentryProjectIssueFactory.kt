package testsupport

import com.boclips.terry.infrastructure.outgoing.sentry.SentryIssueMetadata
import com.boclips.terry.infrastructure.outgoing.sentry.SentryProject
import com.boclips.terry.infrastructure.outgoing.sentry.SentryProjectIssue
import java.time.LocalDateTime

object SentryProjectIssueFactory {
    fun sample(
        project: SentryProject = SentryProject("id", "user-service"),
        count: Int = 5,
        type: String = "NullPointerException",
        title: String = "this is exception title",
        culprit: String = "GET /v1/users",
        permalink: String? = "https://boclips.sentry.com",
        firstSeen: LocalDateTime? = LocalDateTime.now().minusDays(100)
    ) = SentryProjectIssue(project, SentryIssueMetadata(type, title), count, culprit, permalink, firstSeen)
}
