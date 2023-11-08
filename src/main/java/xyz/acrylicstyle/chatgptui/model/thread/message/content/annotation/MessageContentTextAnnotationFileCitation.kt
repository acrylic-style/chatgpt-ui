package xyz.acrylicstyle.chatgptui.model.thread.message.content.annotation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A citation within the message that points to a specific quote from a specific File associated with the assistant or
 * the message. Generated when the assistant uses the "retrieval" tool to search files.
 * @param text The text in the message content that needs to be replaced.
 */
@Serializable
@SerialName("file_citation")
data class MessageContentTextAnnotationFileCitation(
    val text: String,
    @SerialName("file_citation")
    val fileCitation: FileCitationObject,
    @SerialName("start_index")
    val startIndex: Int,
    @SerialName("end_index")
    val endIndex: Int,
) : MessageContentTextAnnotation
