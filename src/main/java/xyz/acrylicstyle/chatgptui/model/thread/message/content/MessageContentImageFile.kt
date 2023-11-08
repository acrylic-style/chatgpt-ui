package xyz.acrylicstyle.chatgptui.model.thread.message.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * References an image File in the content of a message.
 */
@Serializable
@SerialName("image_file")
data class MessageContentImageFile(
    @SerialName("image_file")
    val imageFile: MessageContentImageFileObject,
) : MessageContent
