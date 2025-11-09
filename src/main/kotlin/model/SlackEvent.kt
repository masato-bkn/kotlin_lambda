package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class SlackEvent(
    val type: String,
    val challenge: String? = null,
    val token: String? = null,
    val event: MessageEvent? = null,
    val event_id: String? = null,
)

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
