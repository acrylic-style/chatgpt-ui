package xyz.acrylicstyle.chatgptui.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
sealed interface ContentWithRole {
    fun toJson(): JsonElement
}
