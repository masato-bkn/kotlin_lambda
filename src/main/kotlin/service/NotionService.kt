package com.example.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotionService(
    private val apiKey: String,
    private val pageId: String,
    private val baseUrl: String = "https://api.notion.com",
    private val client: OkHttpClient = OkHttpClient()
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun appendMessage(text: String, user: String, timestamp: String, logger: ((String) -> Unit)? = null) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val requestBody = NotionAppendRequest(
            children = listOf(
                NotionBlock(
                    type = "paragraph",
                    paragraph = ParagraphBlock(
                        richText = listOf(
                            RichText(
                                text = TextContent(
                                    content = "[$now] User: $user\nMessage: $text\nTimestamp: $timestamp"
                                )
                            )
                        )
                    )
                )
            )
        )

        val bodyJson = json.encodeToString(requestBody)
        logger?.invoke("Notion Request Body: $bodyJson")
        logger?.invoke("Notion Page ID: $pageId")

        val request = Request.Builder()
            .url("$baseUrl/v1/blocks/$pageId/children")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Notion-Version", "2022-06-28")
            .patch(bodyJson.toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            logger?.invoke("Notion Response Code: ${response.code}")
            logger?.invoke("Notion Response Body: $responseBody")

            if (!response.isSuccessful) {
                throw Exception("Failed to append to Notion: ${response.code} - $responseBody")
            }
        }
    }
}

@Serializable
data class NotionAppendRequest(
    val children: List<NotionBlock>
)

@Serializable
data class NotionBlock(
    val type: String,
    val paragraph: ParagraphBlock
)

@Serializable
data class ParagraphBlock(
    @SerialName("rich_text")
    val richText: List<RichText>
)

@Serializable
data class RichText(
    val text: TextContent
)

@Serializable
data class TextContent(
    val content: String
)
