package com.preethamm.nova.ai

import com.preethamm.nova.core.config.NovaConfig
import com.preethamm.nova.core.security.SecureApiKeyStore
import com.preethamm.nova.domain.model.ConversationMessage
import com.preethamm.nova.domain.model.NovaIntent
import kotlinx.coroutines.flow.Flow

class ConfigurableAIService(
    private val keyStore: SecureApiKeyStore,
    private val demoAIService: AIService = DemoAIService()
) : AIService {
    override suspend fun sendMessage(
        message: String,
        conversation: List<ConversationMessage>
    ): String = activeService().sendMessage(message, conversation)

    override fun streamResponse(
        message: String,
        conversation: List<ConversationMessage>
    ): Flow<String> = activeService().streamResponse(message, conversation)

    override suspend fun summarize(content: String): String =
        activeService().summarize(content)

    override suspend fun classifyIntent(message: String): NovaIntent =
        activeService().classifyIntent(message)

    private fun activeService(): AIService {
        val apiKey = keyStore.getApiKey()
        return if (apiKey.isNullOrBlank()) {
            demoAIService
        } else {
            OpenAIService(apiKey = apiKey, model = NovaConfig.openAiModel)
        }
    }
}
