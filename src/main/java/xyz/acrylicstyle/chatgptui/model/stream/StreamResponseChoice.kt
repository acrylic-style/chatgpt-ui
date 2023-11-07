package xyz.acrylicstyle.chatgptui.model.stream

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamResponseChoice(
    val delta: StreamResponseChoiceDelta,
    val index: Int,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)
