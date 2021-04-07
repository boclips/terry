package com.boclips.terry.infrastructure.outgoing.slack

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

sealed class PosterResponse

data class PostSuccess(@JsonProperty("ts") val timestamp: BigDecimal) : PosterResponse()
data class PostFailure(val message: String) : PosterResponse()
