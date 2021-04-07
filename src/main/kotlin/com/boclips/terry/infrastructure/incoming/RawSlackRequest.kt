package com.boclips.terry.infrastructure.incoming

import javax.servlet.http.HttpServletRequest

data class RawSlackRequest(
    val currentTime: Long,
    val signatureClaim: String,
    val timestamp: String,
    val body: String
) {
    companion object {
        fun fromRequest(request: HttpServletRequest, time: Long): RawSlackRequest? =
            if (request.getSlackTimestamp() != null && request.getSlackSignature() != null)
                RawSlackRequest(
                    currentTime = time,
                    timestamp = request.getSlackTimestamp(),
                    body = request.reader.use { it.readText() },
                    signatureClaim = request.getSlackSignature()
                ) else null

        private fun HttpServletRequest.getSlackTimestamp() = this.getHeader("X-Slack-Request-Timestamp")
        private fun HttpServletRequest.getSlackSignature() = this.getHeader("X-Slack-Signature")
    }
}
