package xyz.acrylicstyle.chatgptui.model.toolcall

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.toolcall.function.ToolCallFunctionDefinition

/**
 * @param id The ID of the tool call object.
 * @param function The definition of the function that was called.
 */
@Serializable
@SerialName("function")
data class ToolCallFunction(
    val id: String,
    val function: ToolCallFunctionDefinition
) : ToolCall
