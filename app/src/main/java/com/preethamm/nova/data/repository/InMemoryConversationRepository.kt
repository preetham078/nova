package com.preethamm.nova.data.repository

import com.preethamm.nova.domain.model.ConversationMessage
import com.preethamm.nova.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryConversationRepository : ConversationRepository {
    private val mutableMessages = MutableStateFlow<List<ConversationMessage>>(emptyList())

    override val messages: StateFlow<List<ConversationMessage>> = mutableMessages.asStateFlow()

    override suspend fun add(message: ConversationMessage) {
        mutableMessages.value = mutableMessages.value + message
    }

    override suspend fun clear() {
        mutableMessages.value = emptyList()
    }
}
