package com.preethamm.nova.ai

import com.preethamm.nova.domain.model.ConversationMessage
import com.preethamm.nova.domain.model.NovaIntent
import kotlinx.coroutines.flow.Flow

interface AIService {
    suspend fun sendMessage(
        message: String,
        conversation: List<ConversationMessage>
    ): String

    fun streamResponse(
        message: String,
        conversation: List<ConversationMessage>
    ): Flow<String>

    suspend fun summarize(content: String): String

    suspend fun classifyIntent(message: String): NovaIntent
}
