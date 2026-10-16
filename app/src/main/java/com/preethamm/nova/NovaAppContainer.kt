package com.preethamm.nova

import android.content.Context
import com.preethamm.nova.ai.AIService
import com.preethamm.nova.ai.ConfigurableAIService
import com.preethamm.nova.core.security.SecureApiKeyStore
import com.preethamm.nova.data.repository.InMemoryConversationRepository
import com.preethamm.nova.domain.repository.ConversationRepository
import com.preethamm.nova.domain.usecase.SendMessageUseCase
import com.preethamm.nova.voice.AndroidVoiceManager
import com.preethamm.nova.voice.VoiceManager

class NovaAppContainer(context: Context) {
    val apiKeyStore: SecureApiKeyStore = SecureApiKeyStore(context)
    val aiService: AIService = ConfigurableAIService(apiKeyStore)

    val conversationRepository: ConversationRepository = InMemoryConversationRepository()
    val voiceManager: VoiceManager = AndroidVoiceManager(context)

    val sendMessageUseCase: SendMessageUseCase = SendMessageUseCase(
        aiService = aiService,
        repository = conversationRepository,
        isDemoMode = { apiKeyStore.hasApiKey().not() }
    )
}
