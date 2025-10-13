package com.example

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.example.service.GreetingService

class Handler(private val greetingService: GreetingService = GreetingService()) : RequestHandler<Map<String, Any>, Map<String, Any>> {
    override fun handleRequest(input: Map<String, Any>, context: Context): Map<String, Any> {
        val logger = context.logger
        logger.log("Received request: $input")

        val greeting = greetingService.generateGreeting()

        return mapOf(
            "statusCode" to 200,
            "body" to greeting
        )
    }
}
