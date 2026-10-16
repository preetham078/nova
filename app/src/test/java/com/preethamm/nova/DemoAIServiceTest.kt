package com.preethamm.nova

import com.preethamm.nova.ai.DemoAIService
import com.preethamm.nova.domain.model.IntentType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoAIServiceTest {
    private val service = DemoAIService()

    @Test
    fun classifyIntent_detectsEmailSummary() = runTest {
        val intent = service.classifyIntent("Summarize my unread emails")

        assertEquals(IntentType.EMAIL_SUMMARY, intent.type)
        assertTrue(intent.confidence > 0.5)
    }

    @Test
    fun sendMessage_reportsDemoModeWithoutApiKey() = runTest {
        val response = service.sendMessage("Explain polymorphism", emptyList())

        assertTrue(response.contains("DEMO MODE"))
    }
}
