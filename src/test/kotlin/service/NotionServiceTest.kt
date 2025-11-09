package com.example.service

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NotionServiceTest {
    private lateinit var mockServer: MockWebServer
    private lateinit var notionService: NotionService

    @BeforeEach
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()

        val baseUrl = mockServer.url("").toString().removeSuffix("/")
        val client = OkHttpClient()

        notionService = NotionService(
            apiKey = "test-api-key",
            pageId = "test-page-id",
            baseUrl = baseUrl,
            client = client
        )
    }

    @AfterEach
    fun teardown() {
        mockServer.shutdown()
    }

    @Test
    fun `should successfully append message to Notion page`() {
        // Mock successful response
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"object":"block","id":"test-block-id"}""")
        )

        val logs = mutableListOf<String>()
        val logger: (String) -> Unit = { logs.add(it) }

        // Execute the request
        notionService.appendMessage("Test message", "U123456", "1234567890.123456", logger)

        // Verify the request was made
        val request = mockServer.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/v1/blocks/test-page-id/children", request.path)

        // Verify headers
        assertEquals("Bearer test-api-key", request.getHeader("Authorization"))
        assertEquals("application/json; charset=utf-8", request.getHeader("Content-Type"))
        assertEquals("2022-06-28", request.getHeader("Notion-Version"))

        // Verify request body contains the message
        val requestBody = request.body.readUtf8()
        assertTrue(requestBody.contains("Test message"))
        assertTrue(requestBody.contains("U123456"))
        assertTrue(requestBody.contains("1234567890.123456"))
    }

    @Test
    fun `should handle Notion API error gracefully`() {
        // Mock error response
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"object":"error","status":400,"code":"validation_error","message":"Invalid request"}""")
        )

        val logs = mutableListOf<String>()
        val logger: (String) -> Unit = { logs.add(it) }

        // Verify that exception is thrown with proper error message
        val exception = assertThrows(Exception::class.java) {
            notionService.appendMessage("Test message", "U123456", "1234567890.123456", logger)
        }

        assertTrue(exception.message?.contains("Failed to append to Notion") == true)
        assertTrue(exception.message?.contains("400") == true)
    }

    @Test
    fun `should format request body correctly with proper structure`() {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"object":"block","id":"test-block-id"}""")
        )

        val logs = mutableListOf<String>()
        val logger: (String) -> Unit = { logs.add(it) }

        notionService.appendMessage("Hello Notion", "UserA", "1234567890.999", logger)

        // Get the request and parse the body
        val request = mockServer.takeRequest()
        val requestBody = request.body.readUtf8()
        val json = Json.parseToJsonElement(requestBody).jsonObject

        // Verify the structure
        assertTrue(json.containsKey("children"))
        val children = json["children"]?.jsonArray
        assertNotNull(children)
        assertEquals(1, children?.size)

        val block = children?.get(0)?.jsonObject
        assertEquals("paragraph", block?.get("type")?.jsonPrimitive?.content)

        val paragraph = block?.get("paragraph")?.jsonObject
        val richText = paragraph?.get("rich_text")?.jsonArray
        assertNotNull(richText)

        val textContent = richText?.get(0)?.jsonObject?.get("text")?.jsonObject?.get("content")?.jsonPrimitive?.content
        assertNotNull(textContent)
        assertTrue(textContent?.contains("Hello Notion") == true)
        assertTrue(textContent?.contains("UserA") == true)
        assertTrue(textContent?.contains("1234567890.999") == true)
    }
}
