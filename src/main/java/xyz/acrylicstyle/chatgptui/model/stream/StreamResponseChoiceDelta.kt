package xyz.acrylicstyle.chatgptui.model.stream

import kotlinx.serialization.Serializable

@Serializable
data class StreamResponseChoiceDelta(
    val content: String = "",
    val role: String = "assistant",
)
