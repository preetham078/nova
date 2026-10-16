package com.preethamm.nova.domain.model

data class ConversationMessage(
    val id: Long,
    val role: MessageRole,
    val content: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isDemoData: Boolean = false
)

enum class MessageRole {
    USER,
    NOVA,
    SYSTEM
}
