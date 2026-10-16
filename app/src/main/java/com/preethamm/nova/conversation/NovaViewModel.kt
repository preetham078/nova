package com.preethamm.nova.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.preethamm.nova.NovaAppContainer
import com.preethamm.nova.core.security.SecureApiKeyStore
import com.preethamm.nova.domain.model.AssistantState
import com.preethamm.nova.domain.model.ConversationMessage
import com.preethamm.nova.domain.model.MessageRole
import com.preethamm.nova.domain.repository.ConversationRepository
import com.preethamm.nova.domain.usecase.SendMessageUseCase
import com.preethamm.nova.voice.VoiceListener
import com.preethamm.nova.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NovaViewModel(
    private val sendMessage: SendMessageUseCase,
    private val repository: ConversationRepository,
    private val voiceManager: VoiceManager,
    private val apiKeyStore: SecureApiKeyStore
) : ViewModel(), VoiceListener {
    private val mutableUiState = MutableStateFlow(
        NovaUiState(
            demoMode = apiKeyStore.hasApiKey().not(),
            apiKeySaved = apiKeyStore.hasApiKey(),
            statusText = if (apiKeyStore.hasApiKey()) "IDLE" else "IDLE - DEMO MODE"
        )
    )

    val uiState: StateFlow<NovaUiState> = combine(
        mutableUiState,
        repository.messages
    ) { state, messages ->
        state.copy(messages = messages)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = mutableUiState.value
    )

    init {
        voiceManager.setListener(this)
        seedWelcomeMessage()
    }

    fun setMicrophonePermission(granted: Boolean) {
        mutableUiState.update {
            it.copy(
                microphonePermissionGranted = granted,
                errorMessage = if (granted) null else it.errorMessage
            )
        }
    }

    fun onTypedTextChanged(text: String) {
        mutableUiState.update { it.copy(typedText = text) }
    }

    fun onApiKeyDraftChanged(text: String) {
        mutableUiState.update { it.copy(apiKeyDraft = text) }
    }

    fun saveApiKey() {
        apiKeyStore.saveApiKey(uiState.value.apiKeyDraft)
        val hasKey = apiKeyStore.hasApiKey()
        mutableUiState.update {
            it.copy(
                apiKeyDraft = "",
                apiKeySaved = hasKey,
                demoMode = hasKey.not(),
                statusText = if (hasKey) "IDLE" else "IDLE - DEMO MODE",
                errorMessage = if (hasKey) "OpenAI key saved on this device." else "API key was empty."
            )
        }
    }

    fun clearApiKey() {
        apiKeyStore.clearApiKey()
        mutableUiState.update {
            it.copy(
                apiKeyDraft = "",
                apiKeySaved = false,
                demoMode = true,
                statusText = "IDLE - DEMO MODE",
                errorMessage = "OpenAI key cleared from this device."
            )
        }
    }

    fun onMicPressed() {
        if (!uiState.value.microphonePermissionGranted) {
            setError("Microphone permission is required to talk to Nova.")
            return
        }
        mutableUiState.update {
            it.copy(
                assistantState = AssistantState.LISTENING,
                statusText = "LISTENING",
                errorMessage = null
            )
        }
        voiceManager.startListening()
    }

    fun sendTypedMessage() {
        val text = uiState.value.typedText.trim()
        if (text.isBlank()) return
        mutableUiState.update { it.copy(typedText = "") }
        handleUserText(text)
    }

    fun stop() {
        voiceManager.stopListening()
        voiceManager.interruptSpeech()
        mutableUiState.update {
            it.copy(
                assistantState = AssistantState.IDLE,
                statusText = if (apiKeyStore.hasApiKey()) "IDLE" else "IDLE - DEMO MODE"
            )
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            repository.clear()
            seedWelcomeMessage()
        }
    }

    override fun onSpeechReady() {
        mutableUiState.update {
            it.copy(assistantState = AssistantState.LISTENING, statusText = "LISTENING")
        }
    }

    override fun onSpeechStarted() {
        mutableUiState.update {
            it.copy(assistantState = AssistantState.LISTENING, statusText = "LISTENING")
        }
    }

    override fun onSpeechResult(text: String) {
        mutableUiState.update { it.copy(lastHeardText = text) }
        handleUserText(text)
    }

    override fun onSpeechError(message: String) {
        setError(message)
    }

    override fun onSpeechEnded() {
        if (uiState.value.assistantState == AssistantState.LISTENING) {
            mutableUiState.update {
                it.copy(assistantState = AssistantState.THINKING, statusText = "THINKING")
            }
        }
    }

    override fun onTtsStarted() {
        mutableUiState.update {
            it.copy(assistantState = AssistantState.SPEAKING, statusText = "SPEAKING")
        }
    }

    override fun onTtsFinished() {
        mutableUiState.update {
            it.copy(
                assistantState = AssistantState.IDLE,
                statusText = if (apiKeyStore.hasApiKey()) "IDLE" else "IDLE - DEMO MODE"
            )
        }
    }

    override fun onTtsError(message: String) {
        mutableUiState.update {
            it.copy(
                assistantState = AssistantState.IDLE,
                statusText = if (apiKeyStore.hasApiKey()) "IDLE" else "IDLE - DEMO MODE",
                errorMessage = message
            )
        }
    }

    override fun onCleared() {
        voiceManager.shutdown()
        super.onCleared()
    }

    private fun handleUserText(text: String) {
        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    assistantState = AssistantState.THINKING,
                    statusText = "THINKING",
                    errorMessage = null
                )
            }

            runCatching { sendMessage(text) }
                .onSuccess { novaMessage ->
                    mutableUiState.update {
                        it.copy(
                            assistantState = AssistantState.SPEAKING,
                            statusText = "SPEAKING"
                        )
                    }
                    voiceManager.speak(novaMessage.content)
                }
                .onFailure { throwable ->
                    val fallback = "I could not reach the AI service. ${throwable.message.orEmpty()}".trim()
                    repository.add(
                        ConversationMessage(
                            id = System.nanoTime(),
                            role = MessageRole.NOVA,
                            content = fallback
                        )
                    )
                    setError(fallback)
                }
        }
    }

    private fun seedWelcomeMessage() {
        viewModelScope.launch {
            if (repository.messages.value.isEmpty()) {
                repository.add(
                    ConversationMessage(
                        id = System.nanoTime(),
                        role = MessageRole.NOVA,
                        content = "Hi, I am Nova. Tap the microphone and talk to me.",
                        isDemoData = apiKeyStore.hasApiKey().not()
                    )
                )
            }
        }
    }

    private fun setError(message: String) {
        mutableUiState.update {
            it.copy(
                assistantState = AssistantState.ERROR,
                statusText = "ERROR",
                errorMessage = message
            )
        }
    }

    class Factory(
        private val container: NovaAppContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NovaViewModel(
                sendMessage = container.sendMessageUseCase,
                repository = container.conversationRepository,
                voiceManager = container.voiceManager,
                apiKeyStore = container.apiKeyStore
            ) as T
        }
    }
}
