package com.preethamm.nova.conversation

import com.preethamm.nova.domain.model.AssistantState
import com.preethamm.nova.domain.model.ConversationMessage

data class NovaUiState(
    val assistantState: AssistantState = AssistantState.IDLE,
    val messages: List<ConversationMessage> = emptyList(),
    val typedText: String = "",
    val lastHeardText: String = "",
    val statusText: String = "IDLE",
    val errorMessage: String? = null,
    val microphonePermissionGranted: Boolean = false,
    val demoMode: Boolean = true,
    val apiKeyDraft: String = "",
    val apiKeySaved: Boolean = false
)
