package xyz.acrylicstyle.chatgptui.model.thread.message.content.annotation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param fileId The ID of the specific File the citation is from.
 * @param quote The specific quote in the file.
 */
@Serializable
data class FileCitationObject(
    @SerialName("file_id")
    val fileId: String,
    val quote: String,
)
