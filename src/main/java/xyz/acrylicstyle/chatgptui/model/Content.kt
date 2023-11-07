package xyz.acrylicstyle.chatgptui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Content {
    companion object {
        fun text(text: String) = TextContent(text)

        fun image(url: String) = ImageContent(url)
    }
}

@Serializable
@SerialName("text")
data class TextContent(val text: String) : Content

@Serializable
@SerialName("image_url")
data class ImageContent(@SerialName("image_url") val url: String) : Content
