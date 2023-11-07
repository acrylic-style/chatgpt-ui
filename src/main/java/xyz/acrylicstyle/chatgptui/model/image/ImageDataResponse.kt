package xyz.acrylicstyle.chatgptui.model.image

import io.ktor.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageDataResponse(
    @SerialName("b64_json")
    val b64Json: String,
) {
    fun toByteArray(): ByteArray = b64Json.decodeBase64Bytes()
}
