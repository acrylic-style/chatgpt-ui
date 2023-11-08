package xyz.acrylicstyle.chatgptui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListResult<T>(
    val `object`: String,
    val data: List<T>,
    @SerialName("first_id")
    val firstId: String,
    @SerialName("last_id")
    val lastId: String,
    @SerialName("has_more")
    val hasMore: Boolean,
)
