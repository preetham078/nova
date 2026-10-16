package com.preethamm.nova.domain.repository

import com.preethamm.nova.domain.model.ConversationMessage
import kotlinx.coroutines.flow.StateFlow

interface ConversationRepository {
    val messages: StateFlow<List<ConversationMessage>>
    suspend fun add(message: ConversationMessage)
    suspend fun clear()
}
