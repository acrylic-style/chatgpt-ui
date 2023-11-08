package xyz.acrylicstyle.chatgptui.model.run.requiredaction

import kotlinx.serialization.Serializable

/**
 * The function definition.
 * @param name The name of the function.
 * @param arguments The arguments that the model expects you o pass to the function.
 */
@Serializable
data class ToolCallFunctionDefinitionWithoutOutput(val name: String, val arguments: String)
