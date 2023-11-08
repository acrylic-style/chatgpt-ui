package xyz.acrylicstyle.chatgptui.api.assistants.files

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.Range
import xyz.acrylicstyle.chatgptui.api.OpenAI
import xyz.acrylicstyle.chatgptui.model.DeletionStatus
import xyz.acrylicstyle.chatgptui.model.JsonConvertible
import xyz.acrylicstyle.chatgptui.model.JsonConvertible.Companion.encodeToString
import xyz.acrylicstyle.chatgptui.model.ListResult
import xyz.acrylicstyle.chatgptui.model.assistant.Assistant
import xyz.acrylicstyle.chatgptui.model.assistant.AssistantFile
import xyz.acrylicstyle.chatgptui.model.file.File

class AssistantFilesRequest(private val openAI: OpenAI, private val httpClient: HttpClient, assistantId: String) {
    private val baseUrl = "${openAI.baseUrl}/assistants/$assistantId/files"

    /**
     * Create an assistant file by attaching a [File] to an [Assistant].
     * @param fileId A [File] ID (with `purpose="assistants"`) that the assistant should use.
     * Useful for tools like `retrieval` and `code_interpreter` that can access files.
     * @return An [AssistantFile] object.
     */
    suspend fun create(fileId: String) =
        httpClient.post("$baseUrl/$fileId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
            setBody(JsonConvertible.json.encodeToString(mapOf(
                "file_id" to fileId,
            )))
        }.bodyAsText().let { Json.decodeFromString<AssistantFile>(it) }

    /**
     * Retrieves an [AssistantFile].
     * @param fileId The ID of the file we're getting.
     * @return The [AssistantFile] object matching the specified ID.
     */
    suspend fun get(fileId: String) =
        httpClient.get("$baseUrl/$fileId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
        }.bodyAsText().let { Json.decodeFromString<AssistantFile>(it) }

    /**
     * Delete an assistant file.
     * @param fileId The ID of the file to delete.
     * @return Deletion status
     */
    suspend fun delete(fileId: String) =
        httpClient.delete("$baseUrl/$fileId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
        }.bodyAsText().let { Json.decodeFromString<DeletionStatus>(it) }

    /**
     * Returns a list of assistant files.
     * @param limit A limit on the number of objects to be returned. Limit can range between 1 and 100, and the default is 20.
     * @param order Sort order by the `created_at` timestamp of the objects. `asc` for ascending order and `desc` for descending order.
     * @param after A cursor for use in pagination. `after` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your subsequent call can
     * include after=obj_foo in order to fetch the next page of the list.
     * @param before A cursor for use in pagination. `before` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your subsequent call can
     * include before=obj_foo in order to fetch the previous page of the list.
     * @return A list of [AssistantFile] objects.
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
    }.bodyAsText().let { Json.decodeFromString<ListResult<AssistantFile>>(it) }
}
