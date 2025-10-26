package com.example

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.example.model.MessageEvent
import com.example.model.SlackEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class HandlerTest {
    private val handler = Handler()
    private val mockContext: Context = mock()
    private val mockLogger: LambdaLogger = mock()

    init {
        whenever(mockContext.logger).thenReturn(mockLogger)
    }

    @Test
    fun `should handle url_verification event successfully`() {
        val challengeValue = "test_challenge_value_123"

        val slackEvent = SlackEvent(
            type = "url_verification",
            challenge = challengeValue,
            token = "test_token"
        )
        val input = mapOf(
            "body" to Json.encodeToString(slackEvent)
        )

        val response = handler.handleRequest(input, mockContext)
        assertEquals(200, response["statusCode"])

        val responseBody = response["body"] as String
        val json = Json.parseToJsonElement(responseBody).jsonObject
        assertEquals(challengeValue, json["challenge"]?.jsonPrimitive?.content)
    }

    @Test
    fun `should handle event_callback event successfully`() {
        val messageText = "Hello from Slack!"

        val event = MessageEvent(
            type = "message",
            channel = "C123456",
            user = "U123456",
            text = messageText,
            ts = "1234567890.123456"
        )

        val slackEvent = SlackEvent(
            type = "event_callback",
            token = "test_token",
            event = event
        )
        val input = mapOf(
            "body" to Json.encodeToString(slackEvent)
        )

        val response = handler.handleRequest(input, mockContext)
        assertEquals(200, response["statusCode"])

        val responseBody = response["body"] as String
        val json = Json.parseToJsonElement(responseBody).jsonObject
        assertEquals(messageText, json["message"]?.jsonPrimitive?.content)
    }
}
