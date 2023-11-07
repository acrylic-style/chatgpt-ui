package xyz.acrylicstyle.chatgptui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
@SerialName("text")
data class TextContentWithRole(val content: String, val role: String) : ContentWithRole {
    override fun toJson(): JsonElement = Json.encodeToJsonElement(this)
}
