package com.preethamm.nova.ai

import com.preethamm.nova.domain.model.ConversationMessage
import com.preethamm.nova.domain.model.IntentType
import com.preethamm.nova.domain.model.MessageRole
import com.preethamm.nova.domain.model.NovaIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DemoAIService : AIService {
    override suspend fun sendMessage(
        message: String,
        conversation: List<ConversationMessage>
    ): String {
        delay(500)
        val rememberedProject = conversation
            .lastOrNull { item ->
                item.role == MessageRole.USER &&
                    item.content.contains("project", ignoreCase = true) &&
                    item.content.contains("called", ignoreCase = true)
            }
            ?.content
            ?.substringAfter("called", "")
            ?.trim(' ', '.', '!')

        return when {
            message.contains("project", ignoreCase = true) && rememberedProject.isNullOrBlank().not() ->
                "Your project is called $rememberedProject."
            message.contains("email", ignoreCase = true) ->
                "DEMO DATA: I can answer email questions once Gmail is connected in Phase 3. For now, I am ready for AI chat and voice."
            message.startsWith("remember", ignoreCase = true) ->
                "I'll keep that in this conversation context."
            else ->
                "DEMO MODE: I heard \"$message\". Add OPENAI_API_KEY to local.properties for live AI responses."
        }
    }

    override fun streamResponse(
        message: String,
        conversation: List<ConversationMessage>
    ): Flow<String> = flow {
        emit(sendMessage(message, conversation))
    }

    override suspend fun summarize(content: String): String =
        "DEMO SUMMARY: ${content.take(140)}"

    override suspend fun classifyIntent(message: String): NovaIntent {
        val lower = message.lowercase()
        val type = when {
            "email" in lower && ("summarize" in lower || "summary" in lower) -> IntentType.EMAIL_SUMMARY
            "email" in lower -> IntentType.EMAIL_SEARCH
            "message" in lower || "text" in lower -> IntentType.MESSAGE_SEARCH
            "notification" in lower -> IntentType.NOTIFICATION_SEARCH
            "calendar" in lower || "schedule" in lower -> IntentType.CALENDAR_QUERY
            "remind" in lower -> IntentType.REMINDER
            "remember" in lower -> IntentType.PERSONAL_MEMORY
            "weather" in lower || "search" in lower -> IntentType.WEB_SEARCH
            "open " in lower || "launch " in lower -> IntentType.APP_ACTION
            else -> IntentType.GENERAL_AI
        }
        return NovaIntent(type = type, confidence = 0.62)
    }
}
