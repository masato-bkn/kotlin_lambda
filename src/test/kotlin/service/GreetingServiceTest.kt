package com.example.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GreetingServiceTest {
    @Test
    fun `should return greeting message`() {
        val result = GreetingService.generateGreeting()
        assertEquals("Hello World from Kotlin Lambda!", result)
    }
}
