package com.boclips.terry.application

data class SentryReportParams(
    val period: String = "1d",
    val team: String = "engineering",
    val issuesCount: Int = 5,
    val environment: String = "production"
) {
    companion object {
        fun extractFromText(text: String): SentryReportParams {
            val period = getPeriod(text) ?: "1d"
            val issuesCount = getIssuesCount(text) ?: 5
            val team = getTeam(text) ?: "engineering"
            val environment = getEnvironment(text) ?: "production"
            return SentryReportParams(period, team, issuesCount, environment)
        }

        private fun getPeriod(text: String): String? {
            return """.*period ([^ ]+).*""".toRegex().let { pattern ->
                pattern.matchEntire(text)?.groups?.get(1)?.value
            }
        }

        private fun getIssuesCount(text: String): Int? {
            return """.*issues (\d+).*""".toRegex().let { pattern ->
                pattern.matchEntire(text)?.groups?.get(1)?.value?.toInt()
            }
        }

        private fun getTeam(text: String): String? {
            return """.*team (engineering|data)\s?.*""".toRegex().let { pattern ->
                pattern.matchEntire(text)?.groups?.get(1)?.value
            }
        }

        private fun getEnvironment(text: String): String? {
            return """.*environment (production|staging)\s?.*""".toRegex().let { pattern ->
                pattern.matchEntire(text)?.groups?.get(1)?.value
            }
        }
    }
}
