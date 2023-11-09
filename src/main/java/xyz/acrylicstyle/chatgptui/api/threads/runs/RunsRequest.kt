package xyz.acrylicstyle.chatgptui.api.threads.runs

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.Range
import xyz.acrylicstyle.chatgptui.api.OpenAI
import xyz.acrylicstyle.chatgptui.api.threads.runs.steps.StepsRequest
import xyz.acrylicstyle.chatgptui.model.JsonConvertible.Companion.encodeToString
import xyz.acrylicstyle.chatgptui.model.ListResult
import xyz.acrylicstyle.chatgptui.model.run.Run
import xyz.acrylicstyle.chatgptui.model.assistant.tool.AssistantTool
import xyz.acrylicstyle.chatgptui.model.run.ToolOutputs

class RunsRequest(private val openAI: OpenAI, private val httpClient: HttpClient, private val threadId: String) {
    private val baseUrl = "${openAI.baseUrl}/threads/$threadId/runs"

    fun steps(runId: String) = StepsRequest(openAI, httpClient, threadId, runId)

    /**
     * Create a run.
     * @param assistantId The ID of the assistant to use to execute this run.
     * @param model The ID of the Model to be used to execute this run. If a value is provided here, it will override
     * the model associated with the assistant. If not, the model associated with the assistant will be used.
     * @param instructions Override the default system message of the assistant. This is useful for modifying the
     * behavior on a per-run basis.
     * @param tools Override the tools the assistant can use for this run. This is useful for modifying the behavior
     * on a per-run basis.
     * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
     * additional information about the object in a structured format. Keys can be a maximum of 64 characters long
     * and values can be a maximum of 512 characters long.
     * @return A [Run] object.
     */
    suspend fun create(
        assistantId: String,
        model: String? = null,
        instructions: String? = null,
        tools: List<AssistantTool>? = null,
        metadata: Map<String, String>? = null,
    ) = httpClient.post(baseUrl) {
        header("Authorization", "Bearer ${openAI.apiKey}")
        header("OpenAI-Beta", "assistants=v1")
        header("Content-Type", "application/json")
        setBody(mapOf(
            "assistant_id" to assistantId,
            "model" to model,
            "instructions" to instructions,
            "tools" to tools,
            "metadata" to metadata,
        ).encodeToString())
    }.bodyAsText().let {
        try {
            Json.decodeFromString<Run>(it)
        } catch (e: Exception) {
            throw RuntimeException("Failed to decode JSON: $it", e)
        }
    }

    /**
     * Retrieve a run.
     * @param runId The ID of the run to retrieve.
     * @return The [Run] object matching the specified ID.
     */
    suspend fun get(runId: String) =
        httpClient.get("$baseUrl/$runId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
        }.bodyAsText().let { Json.decodeFromString<Run>(it) }

    /**
     * Modifies a run.
     * @param runId The ID of the run to modify.
     * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
     * additional information about the object in a structured format. Keys can be a maximum of 64 characters long
     * and values can be a maximum of 512 characters long.
     * @return The modified [Run] object matching the specified ID.
     */
    suspend fun modify(runId: String, metadata: Map<String, String>? = null) =
        httpClient.post("$baseUrl/$runId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
            header("Content-Type", "application/json")
            setBody(metadata?.let { mapOf("metadata" to it).encodeToString() } ?: "{}")
        }.bodyAsText().let { Json.decodeFromString<Run>(it) }

    /**
     * Returns a list of [Run]s belonging to a thread.
     * @param limit A limit on the number of objects to be returned. Limit can range between 1 and 100, and the default is 20.
     * @param order Sort order by the `created_at` timestamp of the objects. `asc` for ascending order and `desc` for
     * descending order.
     * @param after A cursor for use in pagination. `after` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your subsequent call
     * can include after=obj_foo in order to fetch the next page of the list.
     * @param before A cursor for use in pagination. `before` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your subsequent call
     * can include before=obj_foo in order to fetch the previous page of the list.
     * @return A list of [Run] objects.
     */
    suspend fun list(
        limit: @Range(from = 1, to = 100) Int = 20,
        order: String = "desc",
        after: String? = null,
        before: String? = null,
    ) = httpClient.get(baseUrl) {
        header("Authorization", "Bearer ${openAI.apiKey}")
        header("OpenAI-Beta", "assistants=v1")
        parameter("limit", limit)
        parameter("order", order)
        parameter("after", after)
        parameter("before", before)
    }.bodyAsText().let { Json.decodeFromString<ListResult<Run>>(it) }

    /**
     * When a run has the `status: "requires_action"` and `required_action.type` is `submit_tool_outputs`,
     * this endpoint can be used to submit the outputs from the tool calls once they're all completed.
     * All outputs must be submitted in a single request.
     * @param runId The ID of the run that requires the tool output submission.
     * @param toolOutputs A list of tools for which the outputs are being submitted.
     * @return The modified [Run] object matching the specified ID.
     */
    suspend fun submitToolOutputs(runId: String, toolOutputs: List<ToolOutputs>) =
        httpClient.post("$baseUrl/$runId/submit_tool_outputs") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
            header("Content-Type", "application/json")
            setBody(mapOf(
                "tool_outputs" to toolOutputs,
            ).encodeToString())
        }.bodyAsText().let {
            try {
                Json.decodeFromString<Run>(it)
            } catch (e: Exception) {
                throw RuntimeException("Failed to decode JSON: $it", e)
            }
        }

    /**
     * Cancels a run that is `in_progress`.
     * @param runId The ID of the run to cancel.
     * @return The modified [Run] object matching the specified ID.
     */
    suspend fun cancel(runId: String) =
        httpClient.post("$baseUrl/$runId/cancel") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
        }.bodyAsText().let { Json.decodeFromString<Run>(it) }
}
