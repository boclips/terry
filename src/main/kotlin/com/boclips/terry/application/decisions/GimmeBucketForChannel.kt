package com.boclips.terry.application.decisions

import com.boclips.terry.application.ChannelAlreadyExists
import com.boclips.terry.application.ChannelCreation
import com.boclips.terry.application.ChannelCreationFailed
import com.boclips.terry.application.ChannelCreationSuccess
import com.boclips.terry.application.ChatReply
import com.boclips.terry.application.Decision
import com.boclips.terry.application.InvalidChannelName
import com.boclips.terry.infrastructure.incoming.SlackEvent
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import org.springframework.stereotype.Component

@Component
class GimmeBucketForChannel : WhatToDo {
    override fun isMe(event: SlackEvent): Boolean {
        return extractChannelCreationChannelName(event) != null
    }

    override fun getDecision(event: SlackEvent): Decision {
        val channelName = extractChannelCreationChannelName(event)!!
        return Decision(
            log = "Creating channel $channelName",
            action = ChannelCreation(channelName) { response ->

                val text = when (response) {
                    is ChannelCreationSuccess -> """<@${event.user}> I've created "$channelName"! You can use "@terrybot safenote $channelName" to generate a safenote."""
                    is ChannelAlreadyExists -> """<@${event.user}> A bucket for that channel already exists. You can use "@terrybot safenote $channelName" to generate a safenote if you'd like, or not."""
                    is InvalidChannelName -> """<@${event.user}> "$channelName" is not a valid bucket name 🪣 😤 🤯"""
                    is ChannelCreationFailed -> """<@${event.user}> could not create a bucket at this time for an unknown reason 🤔"""
                }

                ChatReply(
                    slackMessage = SlackMessage(
                        text = text,
                        channel = event.channel
                    )
                )
            }
        )
    }

    private fun extractChannelCreationChannelName(event: SlackEvent): String? {
        return extractChannelName(event.text, """.*(?:bucket|channel)(?: for)? ([a-z0-9-_ ]+).*""")
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
