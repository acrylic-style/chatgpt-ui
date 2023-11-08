package xyz.acrylicstyle.chatgptui.model.run.requiredaction

import kotlinx.serialization.Serializable

/**
 * @param id The ID of the tool call. This ID must be referenced when you submit the tool outputs in using the
 * "Submit tool outputs to run" endpoint.
 * @param type The type of tool call the output is required for. For now, this is always `function`.
 * @param function The function definition.
 */
@Serializable
data class ToolCallOutput(
    val id: String,
    val type: String,
    val function: ToolCallFunctionDefinitionWithoutOutput,
)
