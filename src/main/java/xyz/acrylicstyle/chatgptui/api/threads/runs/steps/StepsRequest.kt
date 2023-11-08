package xyz.acrylicstyle.chatgptui.api.threads.runs.steps

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.Range
import xyz.acrylicstyle.chatgptui.api.OpenAI
import xyz.acrylicstyle.chatgptui.model.ListResult
import xyz.acrylicstyle.chatgptui.model.run.step.RunStep

class StepsRequest(private val openAI: OpenAI, private val httpClient: HttpClient, threadId: String, runId: String) {
    private val baseUrl = "${openAI.baseUrl}/threads/$threadId/runs/$runId/steps"

    /**
     * Retrieves a run step.
     * @param stepId The ID of the run step to retrieve.
     * @return The [RunStep] object matching the specified ID.
     */
    suspend fun get(stepId: String) =
        httpClient.get("$baseUrl/$stepId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
        }.bodyAsText().let { Json.decodeFromString<RunStep>(it) }

    /**
     * Returns a list of run steps belonging to a run.
     * @param limit A limit on the number of objects to be returned. Limit can range between 1 and 100, and the default is 20.
     * @param order Sort order by the `created_at` timestamp of the objects. `asc` for ascending order and `desc` for descending order.
     * @param after A cursor for use in pagination. `after` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your subsequent call
     * can include after=obj_foo in order to fetch the next page of the list.
     * @param before A cursor for use in pagination. `before` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your subsequent call
     * can include before=obj_foo in order to fetch the previous page of the list.
     * @return A list of [RunStep] objects.
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
    }.bodyAsText().let { Json.decodeFromString<ListResult<RunStep>>(it) }
}
