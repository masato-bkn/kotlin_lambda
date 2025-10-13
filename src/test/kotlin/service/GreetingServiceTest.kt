package com.example.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GreetingServiceTest {
    private val service = GreetingService()

    @Test
    fun `should return greeting message`() {
        val result = service.generateGreeting()
        assertEquals("Hello World from Kotlin Lambda!", result)
    }
}
