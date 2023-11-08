package xyz.acrylicstyle.chatgptui.model.thread.message.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The text content that is part of a message.
 */
@Serializable
@SerialName("text")
data class MessageContentText(
    val text: MessageContentTextObject
) : MessageContent
