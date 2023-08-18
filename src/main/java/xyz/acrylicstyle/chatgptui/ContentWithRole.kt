package xyz.acrylicstyle.chatgptui

import kotlinx.serialization.Serializable

@Serializable
data class ContentWithRole(val content: String, val role: String)
