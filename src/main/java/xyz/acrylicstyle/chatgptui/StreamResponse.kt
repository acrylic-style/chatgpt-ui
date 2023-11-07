package xyz.acrylicstyle.chatgptui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StreamResponse(
        val id: String,
        val `object`: String,
        val created: Long,
        val model: String,
        val choices: List<StreamResponseChoice>,
        @SerialName("system_fingerprint")
        val systemFingerprint: String? = null,
)

@Serializable
internal data class StreamResponseChoice(
        val delta: StreamResponseChoiceDelta,
        val index: Int,
        @SerialName("finish_reason")
        val finishReason: String?,
)

@Serializable
internal data class StreamResponseChoiceDelta(
        val content: String = "",
        val role: String = "assistant",
)
