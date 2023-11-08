package xyz.acrylicstyle.chatgptui.model.toolcall

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * @param id The ID of the tool call object.
 * @param retrieval For now, this is always going to be an empty object.
 */
@Serializable
@SerialName("retrieval")
data class ToolCallRetrieval(
    val id: String,
    val retrieval: JsonObject = JsonObject(emptyMap()),
) : ToolCall
