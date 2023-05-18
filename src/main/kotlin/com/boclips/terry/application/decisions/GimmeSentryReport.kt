package com.boclips.terry.application.decisions

import com.boclips.terry.application.ChatReply
import com.boclips.terry.application.Decision
import com.boclips.terry.application.SentryReportCreation
import com.boclips.terry.application.SentryReportParams
import com.boclips.terry.infrastructure.incoming.SlackEvent
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import org.springframework.stereotype.Component

@Component
class GimmeSentryReport : WhatToDo {
    override fun isMe(event: SlackEvent): Boolean {
        return extractSentryReportParams(event) != null
    }

    override fun getDecision(event: SlackEvent): Decision {
        val params = extractSentryReportParams(event)!!
        return Decision(
            log = "Generating sentry report",
            action = SentryReportCreation(params) { response ->
                ChatReply(
                    slackMessage = SlackMessage(
                        channel = event.channel,
                        text =
                        """
                        |🚨 *Sizzling Sentry report - [last ${params.period} / ${params.team} / ${params.environment}]* 🚨 
                        |
                        |Top ${params.issuesCount} unresolved issues: 
                        |
                        |${response.report}
                        """.trimMargin()
                    )
                )
            }
        )
    }

    private fun extractSentryReportParams(event: SlackEvent): SentryReportParams? {
        return SentryReportParams.extractFromText(event.text).takeIf { event.text.lowercase().contains("sentry report") }
    }
}
