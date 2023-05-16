package com.boclips.terry.infrastructure.outgoing

import com.boclips.terry.application.SentryReportParams
import com.boclips.terry.application.SentryReportResponse
import org.springframework.stereotype.Component

@Component
class ComposeSentryReport {

    operator fun invoke(params: SentryReportParams): SentryReportResponse = SentryReportResponse("<REPORT>")
}