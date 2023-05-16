package com.boclips.terry.application.decisions

import com.boclips.terry.application.Decision
import com.boclips.terry.infrastructure.incoming.SlackEvent
import org.springframework.stereotype.Component

@Component
class EventNotificationDecisionMaker(
    private val possibleThingsToDo: List<WhatToDo>,
    private val dunnoWhatToDo: DunnoWhatToDo
) {

    fun tellMeWhatToDo(event: SlackEvent): Decision {
        val whatToDo = possibleThingsToDo.firstOrNull { it.isMe(event) }
        return whatToDo?.getDecision(event) ?: dunnoWhatToDo.printHelp(event)
    }
}