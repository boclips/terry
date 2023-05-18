package com.boclips.terry.application.decisions

import com.boclips.terry.application.Decision
import com.boclips.terry.infrastructure.incoming.SlackEvent

interface WhatToDo {
    fun isMe(event: SlackEvent): Boolean
    fun getDecision(event: SlackEvent): Decision
}