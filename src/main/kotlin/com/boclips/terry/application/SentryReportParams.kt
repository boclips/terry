package com.boclips.terry.application

data class SentryReportParams(
    val periodDays: String = "1d",
    val team: String = "engineering",
    val issuesCount: Int = 5,
    val environment: String = "production"
)
