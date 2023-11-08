package xyz.acrylicstyle.chatgptui.model.toolcall.function

import kotlinx.serialization.Serializable

/**
 * The definition of the function that was called.
 * @param name The name of the function.
 * @param arguments The arguments passed to the function.
 * @param output The output of the function. This will be `null` if the outputs have not been submitted yet.
 */
@Serializable
data class ToolCallFunctionDefinition(
    val name: String,
    val arguments: String,
    val output: String? = null,
)
