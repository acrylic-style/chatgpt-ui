package xyz.acrylicstyle.chatgptui.model.toolcall

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.toolcall.codeinterpreter.ToolCallCodeInterpreterObject

@Serializable
@SerialName("code_interpreter")
data class ToolCallCodeInterpreter(
    val id: String,
    @SerialName("code_interpreter")
    val codeInterpreter: ToolCallCodeInterpreterObject
) : ToolCall
