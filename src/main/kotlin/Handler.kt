package com.example

import arrow.core.Either
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.example.model.*
import com.example.service.NotionWriterService
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class Handler(
    private val notionService: NotionWriterService? = createNotionService()
) : RequestHandler<Map<String, Any>, Map<String, Any>> {
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "#type"
    }

    companion object {
        private fun createNotionService(): NotionWriterService? {
            val apiKey = System.getenv("NOTION_API_KEY")
            val pageId = System.getenv("NOTION_PAGE_ID")
            return if (apiKey != null && pageId != null) {
                NotionWriterService(apiKey, pageId)
            } else {
                null
            }
        }
    }

    override fun handleRequest(input: Map<String, Any>, context: Context): Map<String, Any> {
        val logger = context.logger
        logger.log("Received request: $input")

        return try {
            // Slackのリトライをスキップ
            val headers = input["headers"] as? Map<*, *>
            logger.log("headers: $headers")
            val retryNum = headers?.get("X-Slack-Retry-Num") ?: headers?.get("x-slack-retry-num")
            if (retryNum != null) {
                logger.log("Slackのリトライをスキップ: retry-num=$retryNum")
                return successResponse("{}")
            }

            // リクエストボディを取得
            val body = input["body"] as? String
                ?: return errorResponse("Missing request body", 400)

            // JSONをパース
            val slackEvent = json.decodeFromString<SlackEvent>(body)

            // イベントタイプごとの処理（sealed classでスマートキャスト）
            val responseBody = when (slackEvent) {
                is UrlVerificationEvent -> {
                    logger.log("Parsed Slack event: type=${slackEvent.type}, challenge=${slackEvent.challenge}")
                    json.encodeToString(ChallengeResponse(slackEvent.challenge))
                }
                is EventCallbackEvent -> {
                    logger.log("Parsed Slack event: type=${slackEvent.type}, event_id=${slackEvent.eventId}")
                    val event = slackEvent.event
                    logger.log("受信メッセージ: ${event.text}")

                    // Notionにメッセージを追記
                    notionService?.exec(event.text)?.fold(
                        { error ->
                            logger.log("Notion追記エラー: ${error.message}")
                            return errorResponse("Failed to save to Notion: ${error.message}", 500)
                        },
                        { _ -> logger.log("Notionにメッセージを追記しました") }
                    )

                    json.encodeToString(Response(event.text))
                }
            }
            successResponse(responseBody)
        } catch (e: Exception) {
            logger.log("Error processing request: ${e.message}")
            errorResponse("Invalid request: ${e.message}", 400)
        }
    }

    private fun successResponse(body: String): Map<String, Any> = mapOf(
        "statusCode" to 200,
        "headers" to mapOf("Content-Type" to "application/json"),
        "body" to body
    )

    private fun errorResponse(message: String, statusCode: Int): Map<String, Any> = mapOf(
        "statusCode" to statusCode,
        "headers" to mapOf("Content-Type" to "application/json"),
        "body" to """{"error": "$message"}"""
    )
}
