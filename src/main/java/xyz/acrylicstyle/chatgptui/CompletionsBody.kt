package xyz.acrylicstyle.chatgptui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompletionsBody(
        val model: String,
        @SerialName("max_tokens")
        val maxTokens: Int?,
        val temperature: Double,
        val messages: List<ContentWithRole>,
        val stream: Boolean,
        val user: String?,
)
