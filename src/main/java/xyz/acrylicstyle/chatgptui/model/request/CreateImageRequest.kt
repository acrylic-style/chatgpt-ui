package xyz.acrylicstyle.chatgptui.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateImageRequest(
    val prompt: String,
    val n: Int = 1,
    val size: String = "1024x1024",
    @SerialName("response_format")
    val responseFormat: String,
    val user: String?,
)
