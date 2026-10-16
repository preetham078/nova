package com.preethamm.nova.domain.usecase

import com.preethamm.nova.ai.AIService
import com.preethamm.nova.domain.model.ConversationMessage
import com.preethamm.nova.domain.model.MessageRole
import com.preethamm.nova.domain.repository.ConversationRepository

class SendMessageUseCase(
    private val aiService: AIService,
    private val repository: ConversationRepository,
    private val isDemoMode: () -> Boolean
) {
    suspend operator fun invoke(text: String): ConversationMessage {
        val userMessage = ConversationMessage(
            id = System.nanoTime(),
            role = MessageRole.USER,
            content = text,
            isDemoData = isDemoMode()
        )
        repository.add(userMessage)

        val reply = aiService.sendMessage(text, repository.messages.value)
        val novaMessage = ConversationMessage(
            id = System.nanoTime(),
            role = MessageRole.NOVA,
            content = reply,
            isDemoData = isDemoMode()
        )
        repository.add(novaMessage)
        return novaMessage
    }
}
