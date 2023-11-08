package xyz.acrylicstyle.chatgptui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
@SerialName("vision")
data class JsonContentWithRole(val content: List<Content>, override val role: String) : ContentWithRole {
    override fun toJson(): JsonElement = Json.encodeToJsonElement(this)

    companion object {
        fun fromJson(json: JsonObject): JsonContentWithRole {
            return JsonContentWithRole(
                content = json["content"]!!.jsonArray.map { Content.fromJson(it.jsonObject) },
                role = json["role"]!!.jsonPrimitive.content,
            )
        }
    }
}
