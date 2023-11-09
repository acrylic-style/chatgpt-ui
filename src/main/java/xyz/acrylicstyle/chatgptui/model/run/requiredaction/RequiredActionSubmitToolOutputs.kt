package xyz.acrylicstyle.chatgptui.model.run.requiredaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("submit_tool_outputs")
data class RequiredActionSubmitToolOutputs(
    @SerialName("submit_tool_outputs")
    val submitToolOutputs: RequiredActionSubmitToolOutputsObject,
) : RequiredAction
