package xyz.acrylicstyle.chatgptui.model.run.requiredaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details on the tool outputs needed for this run to continue.
 * @param toolCalls A list of the relevant tool calls.
 */
@Serializable
@SerialName("submit_tool_outputs")
data class RequiredActionSubmitToolOutputs(
    @SerialName("tool_calls")
    val toolCalls: List<ToolCallOutput>,
) : RequiredAction
