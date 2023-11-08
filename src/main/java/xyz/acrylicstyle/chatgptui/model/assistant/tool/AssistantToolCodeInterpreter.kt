package xyz.acrylicstyle.chatgptui.model.assistant.tool

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("code_interpreter")
data object AssistantToolCodeInterpreter : AssistantTool
