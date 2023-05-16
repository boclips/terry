package com.boclips.terry.application.decisions

import com.boclips.terry.application.ChannelUploadCredentialRetrieval
import com.boclips.terry.application.ChatReply
import com.boclips.terry.application.Decision
import com.boclips.terry.infrastructure.incoming.SlackEvent
import com.boclips.terry.infrastructure.outgoing.securecredentials.CredentialNotFound
import com.boclips.terry.infrastructure.outgoing.securecredentials.SafenoteFailure
import com.boclips.terry.infrastructure.outgoing.securecredentials.SecureCredential
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import org.springframework.stereotype.Component

@Component
class GimmeSafenoteForChannel: WhatToDo {
    override fun isMe(event: SlackEvent): Boolean {
        return extractSafenoteChannelName(event) != null
    }

    override fun getDecision(event: SlackEvent): Decision {
        val channelName = extractSafenoteChannelName(event)!!
        return Decision(
            log = "Retrieving safenote for $channelName",
            action = ChannelUploadCredentialRetrieval(channelName) { response ->
                when (response) {
                    is SecureCredential -> ChatReply(
                        slackMessage = SlackMessage(
                            text = """Sure <@${event.user}>, here are the credentials for "$channelName": ${response.url}""",
                            channel = event.channel
                        )
                    )

                    CredentialNotFound -> ChatReply(
                        slackMessage = SlackMessage(
                            text = """Sorry <@${event.user}>, I can't find "$channelName" - maybe check the name?""",
                            channel = event.channel
                        )
                    )

                    is SafenoteFailure -> ChatReply(
                        slackMessage = SlackMessage(
                            text = "Sorry <@${event.user}>, the Safenote service isn't working! Ask an engineer? (${response.message})",
                            channel = event.channel
                        )
                    )
                }
            }
        )
    }

    private fun extractSafenoteChannelName(event: SlackEvent): String? {
       return extractChannelName(event.text, """.*safenote(?: for)? ([a-z0-9-_ ]+).*""")
    }

    private fun extractChannelName(text: String, pattern: String): String? =
        pattern.toRegex(RegexOption.IGNORE_CASE).matchEntire(text)
            ?.groups
            ?.get(1)
            ?.value
            ?.lowercase()
            ?.replace(" please.*$".toRegex(), "")
            ?.replace(" ", "-")
}