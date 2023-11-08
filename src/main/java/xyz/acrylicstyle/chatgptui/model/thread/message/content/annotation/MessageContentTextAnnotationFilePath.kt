package xyz.acrylicstyle.chatgptui.model.thread.message.content.annotation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A URL for the file that's generated when the assistant used the `code_interpreter` tool to generate a file.
 * @param text The text in the message that needs to be replaced.
 */
@Serializable
@SerialName("file_path")
data class MessageContentTextAnnotationFilePath(
    val text: String,
    @SerialName("file_path")
    val filePath: FilePathObject,
    @SerialName("start_index")
    val startIndex: Int,
    @SerialName("end_index")
    val endIndex: Int,
) : MessageContentTextAnnotation
