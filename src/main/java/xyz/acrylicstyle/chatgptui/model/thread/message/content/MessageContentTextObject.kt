package xyz.acrylicstyle.chatgptui.model.thread.message.content

import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.thread.message.content.annotation.MessageContentTextAnnotation

/**
 * @param value The data that makes up the text.
 */
@Serializable
data class MessageContentTextObject(
    val value: String,
    val annotations: List<MessageContentTextAnnotation>,
) : MessageContent
