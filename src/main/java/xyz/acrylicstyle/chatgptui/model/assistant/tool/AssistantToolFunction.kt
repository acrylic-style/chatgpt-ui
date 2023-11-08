package xyz.acrylicstyle.chatgptui.model.assistant.tool

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param function The function definition.
 */
@Serializable
@SerialName("function")
data class AssistantToolFunction(val function: AssistantToolFunctionDefinition) : AssistantTool
