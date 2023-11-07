package xyz.acrylicstyle.chatgptui.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import xyz.acrylicstyle.chatgptui.model.ContentWithRole
import xyz.acrylicstyle.chatgptui.model.TextContentWithRole

@Serializable
data class ChatRequest(
    val model: String,
    @SerialName("max_tokens")
    val maxTokens: Int?,
    val temperature: Double,
    val messages: List<JsonObject>,
    val stream: Boolean,
    val user: String?,
)
