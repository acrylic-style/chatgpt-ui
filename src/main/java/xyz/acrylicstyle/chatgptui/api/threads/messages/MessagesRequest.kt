package xyz.acrylicstyle.chatgptui.api.threads.messages

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.Range
import xyz.acrylicstyle.chatgptui.api.OpenAI
import xyz.acrylicstyle.chatgptui.model.JsonConvertible.Companion.encodeToString
import xyz.acrylicstyle.chatgptui.model.ListResult
import xyz.acrylicstyle.chatgptui.model.thread.message.Message

class MessagesRequest(private val openAI: OpenAI, private val httpClient: HttpClient, threadId: String) {
    private val baseUrl = "${openAI.baseUrl}/threads/$threadId/messages"

    /**
     * Create a message.
     * @param role The role of the entity that is creating the message. Currently only `user` is supported.
     * @param content The content of the message.
     * @param fileIds A list of File IDs that the message should use. There can be a maximum of 10 files attached to
     * a message. Useful for tools like retrieval and code_interpreter that can access and use files.
     * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
     * additional information about the object in a structured format. Keys can be a maximum of 64 characters long
     * and values can be a maximum of 512 characters long.
     * @return A [Message] object.
     */
    suspend fun create(
        role: String,
        content: String,
        fileIds: List<String> = emptyList(),
        metadata: Map<String, String>? = null,
    ) = httpClient.post(baseUrl) {
        header("Authorization", "Bearer ${openAI.apiKey}")
        header("OpenAI-Beta", "assistants=v1")
        header("Content-Type", "application/json")
        setBody(mapOf(
            "role" to role,
            "content" to content,
            "file_ids" to fileIds,
            "metadata" to metadata,
        ).encodeToString())
    }.bodyAsText().let { Json.decodeFromString<Message>(it) }

    /**
     * Retrieve a message.
     * @param messageId The ID of the message to retrieve.
     * @return The [Message] object matching the specified ID.
     */
    suspend fun get(messageId: String) =
        httpClient.get("$baseUrl/$messageId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
        }.bodyAsText().let { Json.decodeFromString<Message>(it) }

    /**
     * Modifies a message.
     * @param messageId The ID of the message to modify.
     * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
     * additional information about the object in a structured format. Keys can be a maximum of 64 characters long
     * and values can be a maximum of 512 characters long.
     * @return The modified [Message] object.
     */
    suspend fun modify(messageId: String, metadata: Map<String, String>? = null) =
        httpClient.post("$baseUrl/$messageId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
            header("Content-Type", "application/json")
            setBody(metadata?.let { mapOf("metadata" to it) } ?: "{}")
        }.bodyAsText().let { Json.decodeFromString<Message>(it) }

    /**
     * Returns a list of messages for a given thread.
     * @param limit A limit on the number of objects to be returned. Limit can range between 1 and 100, and the default
     * is 20.
     * @param order Sort order by the `created_at` timestamp of the objects. `asc` for ascending order and `desc` for
     * descending order.
     * @param after A cursor for use in pagination. `after` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your subsequent call
     * can include after=obj_foo in order to fetch the next page of the list.
     * @param before A cursor for use in pagination. `before` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your subsequent call
     * can include before=obj_foo in order to fetch the previous page of the list.
     * @return A list of [Message] objects.
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
    }.bodyAsText().let { Json.decodeFromString<ListResult<Message>>(it) }
}
