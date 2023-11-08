package xyz.acrylicstyle.chatgptui.model.thread.message.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageContentImageFileObject(
    @SerialName("file_id")
    val fileId: String,
)
