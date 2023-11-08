package xyz.acrylicstyle.chatgptui.api.threads

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThreadCreateBody(val messages: List<ThreadInitialMessage>? = null, val metadata: Map<String, String>? = null) {
    @Serializable
    data class ThreadInitialMessage(
        val role: String,
        val content: String,
        @SerialName("file_ids")
        val fileIds: List<String>? = null,
        val metadata: Map<String, String>? = null,
    )
}
