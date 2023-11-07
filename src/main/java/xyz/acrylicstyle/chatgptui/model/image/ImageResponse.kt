package xyz.acrylicstyle.chatgptui.model.image

import kotlinx.serialization.Serializable

@Serializable
data class ImageResponse(
    val created: Long,
    val data: List<ImageDataResponse>,
)

