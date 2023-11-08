package xyz.acrylicstyle.chatgptui.model.file

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The `File` object represents a document that has been uploaded to OpenAI.
 * @param id The file identifier, which can be referenced in API endpoints.
 * @param bytes The size of the file, in bytes.
 * @param createdAt The Unix timestamp (in seconds) for when the file was created.
 * @param filename The name of the file.
 * @param object The object type, which is always `file`.
 * @param purpose The intended purpose of the file. Supported values are `fine-tune`, `fine-tune-results`,
 * `assistants`, and `assistants_output`.
 */
@Serializable
data class File(
    val id: String,
    val bytes: Int,
    @SerialName("created_at")
    val createdAt: Long,
    val filename: String,
    val `object`: String,
    val purpose: String,
    val status: String? = null,
    @SerialName("status_details")
    val statusDetails: String? = null,
)
