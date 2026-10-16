package com.preethamm.nova.ai

import com.preethamm.nova.domain.model.ConversationMessage
import com.preethamm.nova.domain.model.IntentType
import com.preethamm.nova.domain.model.MessageRole
import com.preethamm.nova.domain.model.NovaIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenAIService(
    private val apiKey: String,
    private val model: String,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : AIService {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun sendMessage(
        message: String,
        conversation: List<ConversationMessage>
    ): String = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("model", model)
            .put("messages", buildMessages(message, conversation))
            .put("temperature", 0.7)
            .toString()
            .toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        httpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("OpenAI request failed (${response.code}): ${responseBody.take(160)}")
            }
            JSONObject(responseBody)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        }
    }

    override fun streamResponse(
        message: String,
        conversation: List<ConversationMessage>
    ): Flow<String> = flow {
        emit(sendMessage(message, conversation))
    }

    override suspend fun summarize(content: String): String =
        sendMessage(
            message = "Summarize this clearly and briefly:\n$content",
            conversation = emptyList()
        )

    override suspend fun classifyIntent(message: String): NovaIntent = withContext(Dispatchers.IO) {
        val prompt = """
            Classify this assistant request as one of:
            GENERAL_AI, EMAIL_SEARCH, EMAIL_SUMMARY, MESSAGE_SEARCH, NOTIFICATION_SEARCH,
            CALENDAR_QUERY, REMINDER, WEB_SEARCH, PERSONAL_MEMORY, APP_ACTION.
            Return compact JSON with keys type, confidence, parameters.

            Request: $message
        """.trimIndent()

        runCatching {
            val raw = sendMessage(prompt, emptyList())
            val json = JSONObject(raw.substringAfter("{").substringBeforeLast("}") + "}")
            NovaIntent(
                type = IntentType.valueOf(json.optString("type", IntentType.GENERAL_AI.name)),
                confidence = json.optDouble("confidence", 0.5),
                parameters = json.optJSONObject("parameters")?.let { params ->
                    params.keys().asSequence().associateWith { key -> params.optString(key) }
                }.orEmpty()
            )
        }.getOrElse {
            NovaIntent(IntentType.GENERAL_AI, confidence = 0.3)
        }
    }

    private fun buildMessages(
        latestMessage: String,
        conversation: List<ConversationMessage>
    ): JSONArray {
        val messages = JSONArray()
        messages.put(
            JSONObject()
                .put("role", "system")
                .put(
                    "content",
                    "You are Nova, a privacy-respecting personal AI voice assistant. Be concise, warm, and never claim access to personal data unless a connected service supplied it."
                )
        )

        conversation.takeLast(16).forEach { item ->
            val role = when (item.role) {
                MessageRole.USER -> "user"
                MessageRole.NOVA -> "assistant"
                MessageRole.SYSTEM -> "system"
            }
            messages.put(JSONObject().put("role", role).put("content", item.content))
        }
        messages.put(JSONObject().put("role", "user").put("content", latestMessage))
        return messages
    }
}
