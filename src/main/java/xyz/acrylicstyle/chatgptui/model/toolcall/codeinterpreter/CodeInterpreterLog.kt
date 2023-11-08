package xyz.acrylicstyle.chatgptui.model.toolcall.codeinterpreter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Text output from the Code Interpreter tool call as part of a run step.
 * @param logs The text output from the Code Interpreter tool call.
 */
@Serializable
@SerialName("logs")
data class CodeInterpreterLog(
    val logs: String,
) : CodeInterpreterOutput
