package xyz.acrylicstyle.chatgptui.model.toolcall.codeinterpreter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Code interpreter image output
 */
@Serializable
@SerialName("image")
data class CodeInterpreterImage(
    val image: CodeInterpreterImageObject,
) : CodeInterpreterOutput
