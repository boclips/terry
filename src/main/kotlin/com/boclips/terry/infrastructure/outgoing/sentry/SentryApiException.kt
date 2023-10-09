package com.boclips.terry.infrastructure.outgoing.sentry

class SentryApiException(message: String) : RuntimeException("Sentry API call failed! $message")
