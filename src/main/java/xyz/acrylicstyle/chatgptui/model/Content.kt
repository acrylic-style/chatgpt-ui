package xyz.acrylicstyle.chatgptui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
sealed interface Content {
    companion object {
        fun text(text: String) = TextContent(text)

        fun image(url: String) = ImageContent(url)

        fun fromJson(json: JsonObject): Content {
            return if (json["text"] != null) {
                text(json["text"]!!.jsonPrimitive.content)
            } else {
                image(json["image_url"]!!.jsonPrimitive.content)
            }
        }
    }
}

@Serializable
@SerialName("text")
data class TextContent(val text: String) : Content

@Serializable
@SerialName("image_url")
data class ImageContent(@SerialName("image_url") val url: String) : Content
