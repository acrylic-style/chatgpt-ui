package xyz.acrylicstyle.chatgptui.model.assistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.file.File

/**
 * A list of [File]s attached to an `assistant`.
 * @param id The identifier, which can be referenced in API endpoints.
 * @param object The object type, which is always `assistant.file`.
 * @param createdAt The Unix timestamp (in seconds) for when the assistant file was created.
 * @param assistantId The assistant ID that the file is attached to.
 */
@Serializable
data class AssistantFile(
    val id: String,
    val `object`: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("assistant_id")
    val assistantId: String,
)
