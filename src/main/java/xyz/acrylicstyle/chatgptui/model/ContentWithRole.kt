package xyz.acrylicstyle.chatgptui.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
sealed interface ContentWithRole : JsonConvertible {
    val role: String

    companion object {
        fun text(content: String) = TextContentWithRole(content, "user")

        fun json(content: List<Content>) = JsonContentWithRole(content, "user")

        fun fromJson(json: JsonObject) =
            if (json["content"] is JsonArray) {
                JsonContentWithRole.fromJson(json)
            } else if (json["content"] is JsonPrimitive) {
                TextContentWithRole.fromJson(json)
            } else {
                error("Unknown content type: ${json["content"]}")
            }
    }
}
