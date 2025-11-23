package com.example.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NotionWriterServiceTest {
    @Test
    fun 引数のメッセージをNotionページに追記するリクエストを送ること() {
        val mockClient = mockk<OkHttpClient>()
        val mockCall = mockk<Call>()
        val mockResponse = mockk<Response>()
        val requestSlot = slot<Request>()
        every { mockClient.newCall(capture(requestSlot)) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true

        val service = NotionWriterService(apiKey = "testApiKey", pageId = "testPageId", client = mockClient)
        service.exec("testMessage")

        val capturedRequest = requestSlot.captured
        val buffer = Buffer()
        capturedRequest.body?.writeTo(buffer)
        val jsonString = buffer.readUtf8()

        assertEquals("https://api.notion.com/v1/blocks/testPageId/children", capturedRequest.url.toString())
        assertEquals("PATCH", capturedRequest.method)
        assertEquals("Bearer testApiKey", capturedRequest.header("Authorization"))
        assertEquals("{\"children\":[{\"type\":\"paragraph\",\"paragraph\":{\"rich_text\":[{\"text\":{\"content\":\"testMessage\"}}]}}]}", jsonString)
        }

    @Test
    fun リクエストが失敗した場合はエラーを返すこと() {
        val mockClient = mockk<OkHttpClient>()
        val mockCall = mockk<Call>()
        val mockResponse = mockk<Response>()
        val requestSlot = slot<Request>()
        every { mockClient.newCall(capture(requestSlot)) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code } returns 401
        every { mockResponse.body } returns null

        val service = NotionWriterService(apiKey = "testApiKey", pageId = "testPageId", client = mockClient)

        val result = service.exec("testMessage").leftOrNull()
        assertNotNull(result)
        assertEquals("Failed to append to Notion: 401 - null", result.message)
    }
}