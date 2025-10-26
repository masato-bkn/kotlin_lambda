package com.example

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.example.model.SlackEvent
import com.example.model.Response
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class Handler : RequestHandler<Map<String, Any>, Map<String, Any>> {
    private val json = Json { ignoreUnknownKeys = true }

    override fun handleRequest(input: Map<String, Any>, context: Context): Map<String, Any> {
        val logger = context.logger
        logger.log("Received request: $input")

        return try {
            // リクエストボディを取得
            val body = input["body"] as? String
                ?: return errorResponse("Missing request body", 400)

            // JSONをパース
            val slackEvent = json.decodeFromString<SlackEvent>(body)
            logger.log("Parsed Slack event: type=${slackEvent.type}")

            // url_verification イベントの処理
            when (slackEvent.type) {
                "event_callback" -> {
                    val response = Response(slackEvent.event.text)
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
