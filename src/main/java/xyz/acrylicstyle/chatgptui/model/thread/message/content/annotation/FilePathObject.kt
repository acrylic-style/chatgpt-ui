package xyz.acrylicstyle.chatgptui.model.thread.message.content.annotation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param fileId The ID of the file that was generated.
 */
@Serializable
data class FilePathObject(
    @SerialName("file_id")
    val fileId: String,
)
