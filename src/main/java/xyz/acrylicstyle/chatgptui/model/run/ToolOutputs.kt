package xyz.acrylicstyle.chatgptui.model.run

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param toolCallId The ID of the tool call in the required_action object within the run object the output is being submitted for.
 * @param output The output of the tool call to be submitted to continue the run.
 */
@Serializable
data class ToolOutputs(
    @SerialName("tool_call_id")
    val toolCallId: String,
    val output: String,
)
