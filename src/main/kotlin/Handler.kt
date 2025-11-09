package com.example

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.example.model.SlackEvent
import com.example.model.ChallengeResponse
import com.example.model.Response
import com.example.service.NotionService
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class Handler : RequestHandler<Map<String, Any>, Map<String, Any>> {
    private val json = Json { ignoreUnknownKeys = true }
    private val notionService: NotionService? by lazy {
        val apiKey = System.getenv("NOTION_API_KEY")
        val pageId = System.getenv("NOTION_PAGE_ID")
        if (apiKey != null && pageId != null) {
            NotionService(apiKey, pageId)
        } else {
            null
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
            logger.log("Parsed Slack event: type=${slackEvent.type}, event_id=${slackEvent.event_id}")

            // イベントタイプごとの処理
            when (slackEvent.type) {
                "url_verification" -> {
                    val challenge = slackEvent.challenge
                        ?: return errorResponse("Missing challenge parameter", 400)

                    val response = ChallengeResponse(challenge)
                    successResponse(json.encodeToString(response))
                }
                "event_callback" -> {
                    val event = slackEvent.event
                        ?: return errorResponse("Missing event", 400)

                    logger.log("受信メッセージ: ${event.text}")

                    // Notionにメッセージを追記
                    try {
                        logger.log("notionService: $notionService")
                        notionService?.appendMessage(event.text, event.user, event.ts) { message ->
                            logger.log(message)
                        }
                        logger.log("Notionにメッセージを追記しました")
                    } catch (e: Exception) {
                        logger.log("Notion追記エラー: ${e.message}")
                        e.printStackTrace()
                    }

                    val response = Response(event.text)
                    successResponse(json.encodeToString(response))
                }
                else -> {
                    errorResponse("Unsupported event type: ${slackEvent.type}", 400)
                }
            }
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
