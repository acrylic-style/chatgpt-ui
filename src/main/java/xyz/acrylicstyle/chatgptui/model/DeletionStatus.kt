package xyz.acrylicstyle.chatgptui.model

import kotlinx.serialization.Serializable

@Serializable
data class DeletionStatus(val id: String, val `object`: String, val deleted: Boolean)
