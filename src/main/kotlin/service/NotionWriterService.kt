package com.example.service

import arrow.core.Either
import arrow.core.raise.either
import com.example.model.Response
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class NotionWriterService(
    private val apiKey: String = System.getenv("NOTION_API_KEY"),
    private val pageId: String = System.getenv("NOTION_PAGE_ID"),
    private val client: OkHttpClient = OkHttpClient()
) {
    fun exec(text: String): Either<Exception, okhttp3.Response> = either {
        val response = client.newCall(buildRequest(text)).execute()
        if (!response.isSuccessful) {
            val responseBody = response.body?.string()
            raise(Exception("Failed to append to Notion: ${response.code} - $responseBody"))
        }
        response
    }

    private fun buildRequest(text: String): Request {
        val json = Json { ignoreUnknownKeys = true }
        val bodyJson = json.encodeToString(buildRequestBody(text))
        return Request.Builder()
            .url("https://api.notion.com/v1/blocks/$pageId/children")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Notion-Version", "2022-06-28")
            .patch(bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
    }

    private fun buildRequestBody(text: String): NotionAppendRequest {
        return NotionAppendRequest(
            children = listOf(
                NotionBlock(
                    type = "paragraph",
                    paragraph = ParagraphBlock(
                        richText = listOf(
                            RichText(
                                text = TextContent(
                                    content = text
                                )
                            )
                        )
                    )
                )
            )
        )
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
}
