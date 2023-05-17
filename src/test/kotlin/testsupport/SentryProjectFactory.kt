package testsupport

import com.boclips.terry.infrastructure.outgoing.sentry.SentryProject

object SentryProjectFactory {
    fun sample(id: String = "1234", slug: String = "user-service") = SentryProject(id, slug)
}
