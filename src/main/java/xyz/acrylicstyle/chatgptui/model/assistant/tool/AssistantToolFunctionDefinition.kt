package xyz.acrylicstyle.chatgptui.model.assistant.tool

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * The function definition.
 * @param description A description of what the function does, used by the model to choose when and how to call the function.
 * @param name The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes,
 * with a maximum length of 64.
 * @param parameters The parameters the functions accepts, described as a JSON Schema object. See the guide for
 * examples, and the JSON Schema reference for documentation about the format.
 * To describe a function that accepts no parameters, provide the value `{"type": "object", "properties": {}}`.
 */
@Serializable
data class AssistantToolFunctionDefinition(
    val description: String,
    val name: String,
    val parameters: JsonObject = JsonObject(emptyMap()),
)
