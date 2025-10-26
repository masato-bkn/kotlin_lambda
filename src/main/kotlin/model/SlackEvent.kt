package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class SlackEvent(
    val type: String,
    val challenge: String? = null,
    val token: String,
    val event: MessageEvent,
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
data class Response(
    val message: String,
)
