package xyz.acrylicstyle.chatgptui.model.run.step.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.toolcall.ToolCall

@Serializable
@SerialName("tool_calls")
data class StepDetailsToolCalls(
    @SerialName("tool_calls")
    val toolCalls: List<ToolCall> = emptyList(),
) : RunStepDetails
