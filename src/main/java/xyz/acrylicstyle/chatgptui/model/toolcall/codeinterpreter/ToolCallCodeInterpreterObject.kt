package xyz.acrylicstyle.chatgptui.model.toolcall.codeinterpreter

import kotlinx.serialization.Serializable

@Serializable
data class ToolCallCodeInterpreterObject(
    val input: String,
    val outputs: List<CodeInterpreterOutput>,
)
