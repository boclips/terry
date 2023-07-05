package com.boclips.terry.application.decisions

import com.boclips.terry.application.ChatReply
import com.boclips.terry.application.Decision
import com.boclips.terry.infrastructure.incoming.SlackEvent
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import org.springframework.stereotype.Component

@Component
class DunnoWhatToDo {

    fun printHelp(event: SlackEvent) : Decision {
        return Decision(
            log = "Responding via chat with \"${helpFor(event.user)}\"",
            action = ChatReply(
                slackMessage = SlackMessage(
                    channel = event.channel,
                    text = helpFor(event.user)
                )
            )
        )
    }

    private fun helpFor(user: String): String = "<@$user> ${help()}"

    private fun help(): String = """
        Some things you can do:
        video 1234 (retrieves a video and displays a menu)
        safenote a-channel-name (retrieves a new Safenote for an existing channel's upload credentials)
        bucket for a-channel-name (creates a bucket in AWS for a-channel-name)
        sentry report [issues 1|5|100|etc] [environment staging|production] [team engineering|data] [period 1h|1d|etc|] [threshold 1|2|etc]
    """.trimIndent()
}