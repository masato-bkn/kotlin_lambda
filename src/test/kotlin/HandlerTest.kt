package com.example

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.example.model.*
import com.example.service.NotionService
import io.mockk.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class HandlerTest {
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "#type"
    }
    private val handler = Handler()
    private val mockContext: Context = mock()
    private val mockLogger: LambdaLogger = mock()

    init {
        whenever(mockContext.logger).thenReturn(mockLogger)
    }

    @Test
    fun `should handle url_verification event successfully`() {
        val challengeValue = "test_challenge_value_123"

        val slackEvent = UrlVerificationEvent(
            challenge = challengeValue,
            token = "test_token"
        )
        val input = mapOf(
            "body" to json.encodeToString<SlackEvent>(slackEvent)
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

        val slackEvent = EventCallbackEvent(
            token = "test_token",
            event = event
        )
        val input = mapOf(
            "body" to json.encodeToString<SlackEvent>(slackEvent)
        )

        val response = handler.handleRequest(input, mockContext)
        assertEquals(200, response["statusCode"])

        val responseBody = response["body"] as String
        val json = Json.parseToJsonElement(responseBody).jsonObject
        assertEquals(messageText, json["message"]?.jsonPrimitive?.content)
    }

    @Test
    fun `should skip Slack retry requests with X-Slack-Retry-Num header`() {
        val slackEvent = EventCallbackEvent(
            token = "test_token",
            event = MessageEvent(
                type = "message",
                channel = "C123456",
                user = "U123456",
                text = "Test message",
                ts = "1234567890.123456"
            )
        )
        val input = mapOf(
            "headers" to mapOf("X-Slack-Retry-Num" to "1"),
            "body" to json.encodeToString<SlackEvent>(slackEvent)
        )

        val response = handler.handleRequest(input, mockContext)
        assertEquals(200, response["statusCode"])

        val responseBody = response["body"] as String
        assertEquals("{}", responseBody)
    }

    @Test
    fun `should skip Slack retry requests with lowercase x-slack-retry-num header`() {
        val slackEvent = EventCallbackEvent(
            token = "test_token",
            event = MessageEvent(
                type = "message",
                channel = "C123456",
                user = "U123456",
                text = "Test message",
                ts = "1234567890.123456"
            )
        )
        val input = mapOf(
            "headers" to mapOf("x-slack-retry-num" to "2"),
            "body" to json.encodeToString<SlackEvent>(slackEvent)
        )

        val response = handler.handleRequest(input, mockContext)
        assertEquals(200, response["statusCode"])

        val responseBody = response["body"] as String
        assertEquals("{}", responseBody)
    }

    @Test
    fun `should call NotionService when handling event_callback`() {
        val mockNotionService = mockk<NotionService>()

        // Setup mock behavior
        every { mockNotionService.appendMessage(any(), any(), any(), any()) } just Runs

        val handlerWithNotion = Handler(mockNotionService)

        val messageText = "Hello from Slack!"
        val event = MessageEvent(
            type = "message",
            channel = "C123456",
            user = "U123456",
            text = messageText,
            ts = "1234567890.123456"
        )

        val slackEvent = EventCallbackEvent(
            token = "test_token",
            event = event
        )
        val input = mapOf(
            "body" to json.encodeToString<SlackEvent>(slackEvent)
        )

        val response = handlerWithNotion.handleRequest(input, mockContext)
        assertEquals(200, response["statusCode"])

        // Verify NotionService.appendMessage was called with correct parameters
        verify {
            mockNotionService.appendMessage(
                messageText,
                "U123456",
                "1234567890.123456",
                any()
            )
        }
    }
}
