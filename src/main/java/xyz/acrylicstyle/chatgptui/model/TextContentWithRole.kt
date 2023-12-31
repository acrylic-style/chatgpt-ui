package xyz.acrylicstyle.chatgptui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
@SerialName("text")
data class TextContentWithRole(val content: String, override val role: String) : ContentWithRole {
    override fun toJson(): JsonElement = Json.encodeToJsonElement(this)

    companion object {
        fun fromJson(json: JsonObject): TextContentWithRole {
            return TextContentWithRole(
                content = json["content"]!!.jsonPrimitive.content,
                role = json["role"]!!.jsonPrimitive.content,
            )
        }
    }
}
