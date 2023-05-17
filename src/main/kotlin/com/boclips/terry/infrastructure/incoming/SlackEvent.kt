package com.boclips.terry.infrastructure.incoming

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = AppMention::class,
        name = "app_mention"
    )
)
@JsonIgnoreProperties(ignoreUnknown = true)
sealed class SlackEvent

data class AppMention(
    val type: String,

    val user: String,

    val text: String,

    val ts: String,

    val channel: String,

    @JsonProperty("event_ts")
    val eventTs: String
) : SlackEvent()
