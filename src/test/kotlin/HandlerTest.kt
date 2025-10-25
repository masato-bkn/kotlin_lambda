package com.example

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
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
        // Given
        val challengeValue = "test_challenge_value_123"
        val requestBody = """
            {
                "type": "url_verification",
                "challenge": "$challengeValue",
                "token": "test_token"
            }
        """.trimIndent()

        val input = mapOf(
            "body" to requestBody
        )

        // When
        val response = handler.handleRequest(input, mockContext)

        // Then
        assertEquals(200, response["statusCode"])

        val responseBody = response["body"] as String
        val json = Json.parseToJsonElement(responseBody).jsonObject
        assertEquals(challengeValue, json["challenge"]?.jsonPrimitive?.content)
    }

    @Test
    fun `should return 400 when body is missing`() {
        // Given
        val input = mapOf<String, Any>()

        // When
        val response = handler.handleRequest(input, mockContext)

        // Then
        assertEquals(400, response["statusCode"])

        val responseBody = response["body"] as String
        assertTrue(responseBody.contains("error"))
    }

    @Test
    fun `should return 400 for unsupported event type`() {
        // Given
        val requestBody = """
            {
                "type": "unsupported_event",
                "data": "some_data"
            }
        """.trimIndent()

        val input = mapOf(
            "body" to requestBody
        )

        // When
        val response = handler.handleRequest(input, mockContext)

        // Then
        assertEquals(400, response["statusCode"])

        val responseBody = response["body"] as String
        assertTrue(responseBody.contains("Unsupported event type"))
    }

    @Test
    fun `should return 400 when challenge is missing in url_verification event`() {
        // Given
        val requestBody = """
            {
                "type": "url_verification",
                "token": "test_token"
            }
        """.trimIndent()

        val input = mapOf(
            "body" to requestBody
        )

        // When
        val response = handler.handleRequest(input, mockContext)

        // Then
        assertEquals(400, response["statusCode"])

        val responseBody = response["body"] as String
        assertTrue(responseBody.contains("Missing challenge parameter"))
    }

    @Test
    fun `should return 400 for invalid JSON`() {
        // Given
        val requestBody = "invalid json"

        val input = mapOf(
            "body" to requestBody
        )

        // When
        val response = handler.handleRequest(input, mockContext)

        // Then
        assertEquals(400, response["statusCode"])

        val responseBody = response["body"] as String
        assertTrue(responseBody.contains("error"))
    }
}
