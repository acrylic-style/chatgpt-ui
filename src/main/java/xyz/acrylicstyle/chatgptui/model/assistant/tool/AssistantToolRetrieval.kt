package xyz.acrylicstyle.chatgptui.model.assistant.tool

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("retrieval")
data object AssistantToolRetrieval : AssistantTool
