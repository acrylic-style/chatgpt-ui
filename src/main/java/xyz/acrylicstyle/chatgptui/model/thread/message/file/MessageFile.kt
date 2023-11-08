package xyz.acrylicstyle.chatgptui.model.thread.message.file

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.file.File
import xyz.acrylicstyle.chatgptui.model.thread.message.Message

/**
 * A list of files attached to a [Message].
 * @param id The identifier, which can be referenced in API endpoints.
 * @param object The object type, which is always `thread.message.file`.
 * @param createdAt The Unix timestamp (in seconds) for when the file was created.
 * @param messageId The ID of the [Message] that the [File] is attached to.
 * @param fileId The ID of the [File].
 */
@Serializable
data class MessageFile(
    val id: String,
    val `object`: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("message_id")
    val messageId: String,
    @SerialName("file_id")
    val fileId: String,
)
