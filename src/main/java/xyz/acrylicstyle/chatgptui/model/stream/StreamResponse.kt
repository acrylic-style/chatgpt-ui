package xyz.acrylicstyle.chatgptui.model.stream

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<StreamResponseChoice>,
    @SerialName("system_fingerprint")
    val systemFingerprint: String? = null,
)

