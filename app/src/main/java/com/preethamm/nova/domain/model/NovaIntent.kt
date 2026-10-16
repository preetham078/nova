package com.preethamm.nova.domain.model

data class NovaIntent(
    val type: IntentType,
    val confidence: Double,
    val parameters: Map<String, String> = emptyMap()
)
