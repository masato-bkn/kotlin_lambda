package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class SlackEvent(
    val type: String,
    val challenge: String? = null,
    val token: String? = null
)

@Serializable
data class ChallengeResponse(
    val challenge: String
)
