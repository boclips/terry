package com.boclips.terry.presentation

sealed class ControllerResponse

object Success : ControllerResponse()
object Failure : ControllerResponse()
class SlackVerificationResponse(val challenge: String) : ControllerResponse()
