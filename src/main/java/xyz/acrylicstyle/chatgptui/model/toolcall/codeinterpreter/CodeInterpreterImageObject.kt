package xyz.acrylicstyle.chatgptui.model.toolcall.codeinterpreter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.file.File

/**
 * @param fileId The [File] ID of the image.
 */
@Serializable
data class CodeInterpreterImageObject(
    @SerialName("file_id")
    val fileId: String,
)
