package xyz.acrylicstyle.chatgptui.model.thread

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.thread.message.Message

/**
 * Represents a thread that contains [Message]s.
 * @param id The identifier, which can be referenced in API endpoints.
 * @param object The object type, which is always `thread`.
 * @param createdAt The Unix timestamp (in seconds) for when the thread was created.
 * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
 * additional information about the object in a structured format. Keys can be a maximum of 64 characters long
 * and values can be a maximum of 512 characters long.
 */
@Serializable
data class ThreadObject(
    val id: String,
    val `object`: String, // always `thread`
    @SerialName("created_at")
    val createdAt: Long,
    val metadata: Map<String, String>,
)
