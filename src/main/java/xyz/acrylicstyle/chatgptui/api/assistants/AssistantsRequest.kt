package xyz.acrylicstyle.chatgptui.api.assistants

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.Range
import xyz.acrylicstyle.chatgptui.api.OpenAI
import xyz.acrylicstyle.chatgptui.api.assistants.files.AssistantFilesRequest
import xyz.acrylicstyle.chatgptui.model.assistant.Assistant
import xyz.acrylicstyle.chatgptui.model.assistant.tool.AssistantTool
import xyz.acrylicstyle.chatgptui.model.file.File
import xyz.acrylicstyle.chatgptui.model.DeletionStatus
import xyz.acrylicstyle.chatgptui.model.JsonConvertible
import xyz.acrylicstyle.chatgptui.model.JsonConvertible.Companion.encodeToString
import xyz.acrylicstyle.chatgptui.model.ListResult

class AssistantsRequest(private val openAI: OpenAI, private val httpClient: HttpClient) {
    fun files(assistantId: String) = AssistantFilesRequest(openAI, httpClient, assistantId)

    /**
     * Create an assistant with a model and instructions.
     * @param model ID of the model to use. You can use the List models API to see all of your available models, or see
     * Model overview for descriptions of them.
     * @param name The name of the assistant. The maximum length is 256 characters.
     * @param description The description of the assistant. The maximum length is 512 characters.
     * @param instructions The system instructions that the assistant uses. The maximum length is 32768 characters.
     * @param tools A list of tool enabled on the assistant. There can be a maximum of 128 tools per assistant.
     * Tools can be of types `code_interpreter`, `retrieval`, or `function`.
     * @param fileIds A list of [File] IDs attached to this assistant. There can be a maximum of 20 files attached to
     * the assistant. Files are ordered by their creation date in ascending order.
     * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
     * additional information about the object in a structured format. Keys can be a maximum of 64 characters long and
     * values can be a maximum of 512 characters long.
     * @return An [Assistant] object.
     */
    suspend fun create(
        model: String,
        name: String? = null,
        description: String? = null,
        instructions: String? = null,
        tools: List<AssistantTool> = emptyList(),
        fileIds: List<String> = emptyList(),
        metadata: Map<String, String> = emptyMap(),
    ) = httpClient.post("${openAI.baseUrl}/assistants") {
        header("Authorization", "Bearer ${openAI.apiKey}")
        header("OpenAI-Beta", "assistants=v1")
        header("Content-Type", "application/json")
        setBody(JsonConvertible.json.encodeToString(mapOf(
            "model" to model,
            "name" to name,
            "description" to description,
            "instructions" to instructions,
            "tools" to tools,
            "file_ids" to fileIds,
            "metadata" to metadata,
        )))
    }.bodyAsText().let { Json.decodeFromString<Assistant>(it) }

    /**
     * Retrieves an assistant.
     * @param assistantId The ID of the assistant to retrieve.
     * @return The [Assistant] object matching the specified ID.
     */
    suspend fun get(assistantId: String) =
        httpClient.get("${openAI.baseUrl}/assistants/$assistantId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
        }.bodyAsText().let { Json.decodeFromString<Assistant>(it) }

    /**
     * Modifies an assistant.
     * @param assistantId The ID of the assistant to modify.
     * @param model ID of the model to use. You can use the List models API to see all of your available models, or see
     * Model overview for descriptions of them.
     * @param name The name of the assistant. The maximum length is 256 characters.
     * @param description The description of the assistant. The maximum length is 512 characters.
     * @param instructions The system instructions that the assistant uses. The maximum length is 32768 characters.
     * @param tools A list of tool enabled on the assistant. There can be a maximum of 128 tools per assistant.
     * Tools can be of types `code_interpreter`, `retrieval`, or `function`.
     * @param fileIds A list of [File] IDs attached to this assistant. There can be a maximum of 20 files attached to
     * the assistant. Files are ordered by their creation date in ascending order. If a file was previously attached
     * to the list but does not show up in the list, it will be deleted from the assistant.
     * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
     * additional information about the object in a structured format. Keys can be a maximum of 64 characters long
     * and values can be a maximum of 512 characters long.
     * @return The modified [Assistant] object.
     */
    suspend fun modify(
        assistantId: String,
        model: String? = null,
        name: String? = null,
        description: String? = null,
        instructions: String? = null,
        tools: List<AssistantTool> = emptyList(),
        fileIds: List<String> = emptyList(),
        metadata: Map<String, String> = emptyMap(),
    ) = httpClient.post("${openAI.baseUrl}/assistants/$assistantId") {
        header("Authorization", "Bearer ${openAI.apiKey}")
        header("OpenAI-Beta", "assistants=v1")
        header("Content-Type", "application/json")
        setBody(JsonConvertible.json.encodeToString(mapOf(
            "model" to model,
            "name" to name,
            "description" to description,
            "instructions" to instructions,
            "tools" to tools,
            "file_ids" to fileIds,
            "metadata" to metadata,
        )))
    }.bodyAsText().let { Json.decodeFromString<Assistant>(it) }

    /**
     * Delete an assistant.
     * @param assistantId The ID of the assistant to delete.
     * @return Deletion status
     */
    suspend fun delete(assistantId: String) =
        httpClient.delete("${openAI.baseUrl}/assistants/$assistantId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
        }.bodyAsText().let { Json.decodeFromString<DeletionStatus>(it) }

    /**
     * Returns a list of assistants.
     * @param limit A limit on the number of objects to be returned. Limit can range between 1 and 100, and the default is 20.
     * @param order Sort order by the `created_at` timestamp of the objects. `asc` for ascending order and `desc` for descending order.
     * @param after A cursor for use in pagination. `after` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your subsequent call can
     * include after=obj_foo in order to fetch the next page of the list.
     * @param before A cursor for use in pagination. `before` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your subsequent call can
     * include before=obj_foo in order to fetch the previous page of the list.
     * @return A list of [Assistant] objects.
     */
    suspend fun list(
        limit: @Range(from = 1, to = 100) Int = 20,
        order: String = "desc",
        after: String? = null,
        before: String? = null,
    ) = httpClient.get("${openAI.baseUrl}/assistants") {
        header("Authorization", "Bearer ${openAI.apiKey}")
        header("OpenAI-Beta", "assistants=v1")
        parameter("limit", limit)
        parameter("order", order)
        parameter("after", after)
        parameter("before", before)
    }.bodyAsText().let { Json.decodeFromString<ListResult<Assistant>>(it) }
}
