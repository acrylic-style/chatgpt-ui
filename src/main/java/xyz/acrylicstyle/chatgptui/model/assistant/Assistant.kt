package xyz.acrylicstyle.chatgptui.model.assistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.assistant.tool.AssistantTool
import xyz.acrylicstyle.chatgptui.model.file.File

/**
 * Represents an `assistant` that can call the model and use tools.
 * @param id The identifier, which can be referenced in API endpoints.
 * @param object The object type, which is always `assistant`.
 * @param createdAt The Unix timestamp (in seconds) for when the assistant was created.
 * @param name The name of the assistant. The maximum length is 256 characters.
 * @param description The description of the assistant. The maximum length is 512 characters.
 * @param model ID of the model to use. You can use the List models API to see all of your available models, or see
 * Model overview for descriptions of them.
 * @param instructions The system instructions that the assistant uses. The maximum length is 32768 characters.
 * @param tools A list of tool enabled on the assistant. There can be a maximum of 128 tools per assistant.
 * Tools can be of types `code_interpreter`, `retrieval`, or `function`.
 * @param fileIds A list of [File] IDs attached to this assistant. There can be a maximum of 20 files attached to the
 * assistant. Files are ordered by their creation date in ascending order.
 * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
 * additional information about the object in a structured format. Keys can be a maximum of 64 characters long and
 * values can be a maximum of 512 characters long.
 */
@Serializable
data class Assistant(
    val id: String,
    val `object`: String,
    @SerialName("created_at")
    val createdAt: String,
    val name: String? = null,
    val description: String? = null,
    val model: String,
    val instructions: String? = null,
    val tools: List<AssistantTool> = emptyList(),
    @SerialName("file_ids")
    val fileIds: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)
