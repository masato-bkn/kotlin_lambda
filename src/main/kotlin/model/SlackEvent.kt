package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed class SlackEvent {
    abstract val type: String
    abstract val token: String?
}

@Serializable
@SerialName("url_verification")
data class UrlVerificationEvent(
    override val type: String = "url_verification",
    override val token: String? = null,
    val challenge: String
) : SlackEvent()

@Serializable
@SerialName("event_callback")
data class EventCallbackEvent(
    override val type: String = "event_callback",
    override val token: String?,
    val event: MessageEvent,
    @SerialName("event_id")
    val eventId: String? = null
) : SlackEvent()

@Serializable
data class MessageEvent(
    val type: String,
    val channel: String,
    val user: String,
    val text: String,
    val ts: String,
)

@Serializable
data class ChallengeResponse(
    val challenge: String,
)

@Serializable
data class Response(
    val message: String,
)
