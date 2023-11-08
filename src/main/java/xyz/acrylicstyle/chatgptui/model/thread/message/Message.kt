package xyz.acrylicstyle.chatgptui.model.thread.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.thread.ThreadObject
import xyz.acrylicstyle.chatgptui.model.thread.message.content.MessageContent

/**
 * Represents a message in a [ThreadObject].
 * @param id The identifier, which can be referenced in API endpoints.
 * @param object The object type, which is always `thread.message`.
 * @param createdAt The Unix timestamp (in seconds) for when the message was created.
 * @param threadId The [ThreadObject] ID that this message belongs to.
 * @param role The entity that produced the message. One of `user` or `assistant`.
 * @param content The content of the message in array of text and/or images.
 * @param assistantId If applicable, the ID of the assistant that authored the message.
 * @param runId If applicable, the ID of the run associated with the authoring of this message.
 * @param fileIds A list of file IDs that the assistant should use. Useful for tools like retrieval and
 * code_interpreter that can access files. A maximum of 10 files can be attached to a message.
 * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
 * additional information about the object in a structured format. Keys can be a maximum of 64 characters long
 * and values can be a maximum of 512 characters long.
 */
@Serializable
data class Message(
    val id: String,
    val `object`: String, // always `thread.message`
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("thread_id")
    val threadId: String,
    val role: String,
    val content: List<MessageContent>,
    @SerialName("assistant_id")
    val assistantId: String? = null,
    @SerialName("run_id")
    val runId: String? = null,
    @SerialName("file_ids")
    val fileIds: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)
