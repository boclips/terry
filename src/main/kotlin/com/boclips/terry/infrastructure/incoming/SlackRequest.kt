package com.boclips.terry.infrastructure.incoming

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.Date

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = VerificationRequest::class,
        name = "url_verification"
    ),
    JsonSubTypes.Type(
        value = EventNotification::class,
        name = "event_callback"
    ),
    JsonSubTypes.Type(
        value = BlockActions::class,
        name = "block_actions"
    )
)
@JsonIgnoreProperties(ignoreUnknown = true)
sealed class SlackRequest

object Malformed : SlackRequest()

data class VerificationRequest(
    val challenge: String,
    val type: String
) : SlackRequest()

data class EventNotification(
    @JsonProperty("team_id")
    val teamId: String?,

    @JsonProperty("api_app_id")
    val apiAppId: String?,

    val event: SlackEvent,

    val type: String,

    @JsonProperty("authed_users")
    val authedUsers: List<String>?,

    @JsonProperty("event_id")
    val eventId: String?,

    @JsonProperty("event_time")
    val eventTime: Date?
) : SlackRequest()

data class BlockActions(
    val channel: BlockActionIdentifiable,
    val actions: List<BlockAction>,
    val user: BlockActionIdentifiable,
    @JsonProperty("response_url")
    val responseUrl: String
) : SlackRequest()

@JsonIgnoreProperties(ignoreUnknown = true)
data class BlockActionIdentifiable(
    val id: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BlockAction(
    @JsonProperty("selected_option")
    val selectedOption: BlockActionSelectedOption
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BlockActionSelectedOption(
    val value: String
)
