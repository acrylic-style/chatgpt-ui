package xyz.acrylicstyle.chatgptui.api.threads

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import xyz.acrylicstyle.chatgptui.api.OpenAI
import xyz.acrylicstyle.chatgptui.api.threads.messages.MessagesRequest
import xyz.acrylicstyle.chatgptui.api.threads.runs.RunsRequest
import xyz.acrylicstyle.chatgptui.model.DeletionStatus
import xyz.acrylicstyle.chatgptui.model.JsonConvertible.Companion.encodeToString
import xyz.acrylicstyle.chatgptui.model.JsonConvertible.Companion.json
import xyz.acrylicstyle.chatgptui.model.assistant.tool.AssistantTool
import xyz.acrylicstyle.chatgptui.model.run.Run
import xyz.acrylicstyle.chatgptui.model.thread.ThreadObject
import xyz.acrylicstyle.chatgptui.util.toJsonElement

class ThreadsRequest(private val openAI: OpenAI, private val httpClient: HttpClient) {
    fun messages(threadId: String) = MessagesRequest(openAI, httpClient, threadId)
    fun runs(threadId: String) = RunsRequest(openAI, httpClient, threadId)

    suspend fun create(messages: List<ThreadCreateBody.ThreadInitialMessage>? = null, metadata: Map<String, String>? = null) =
        httpClient.post("${openAI.baseUrl}/threads") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
            header("Content-Type", "application/json")
            setBody(ThreadCreateBody(messages, metadata).encodeToString())
        }.bodyAsText().let { Json.decodeFromString<ThreadObject>(it) }

    suspend fun get(threadId: String) =
        httpClient.get("${openAI.baseUrl}/threads/$threadId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
        }.bodyAsText().let { Json.decodeFromString<ThreadObject>(it) }

    suspend fun modify(threadId: String, metadata: Map<String, String>? = null) =
        httpClient.post("${openAI.baseUrl}/threads/$threadId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
            header("Content-Type", "application/json")
            setBody(metadata?.let { mapOf("metadata" to it).encodeToString() } ?: "{}")
        }.bodyAsText().let { Json.decodeFromString<ThreadObject>(it) }

    suspend fun delete(threadId: String) =
        httpClient.delete("${openAI.baseUrl}/threads/$threadId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            header("OpenAI-Beta", "assistants=v1")
        }.bodyAsText().let { Json.decodeFromString<DeletionStatus>(it) }

    suspend fun createAndRun(
        assistantId: String,
        threadMessages: List<ThreadCreateBody.ThreadInitialMessage>? = null,
        threadMetadata: Map<String, String>? = null,
        model: String? = null,
        instructions: String? = null,
        tools: List<AssistantTool>? = null,
        metadata: Map<String, String>? = null,
    ) = httpClient.post("${openAI.baseUrl}/threads/runs") {
        header("Authorization", "Bearer ${openAI.apiKey}")
        header("OpenAI-Beta", "assistants=v1")
        header("Content-Type", "application/json")
        setBody(mapOf(
            "assistant_id" to assistantId,
            "thread" to mapOf(
                "messages" to threadMessages,
                "metadata" to threadMetadata,
            ),
            "model" to model,
            "instructions" to instructions,
            "tools" to tools,
            "metadata" to metadata,
        ).toJsonElement().encodeToString().apply { println(this) })
    }.bodyAsText().let {
        try {
            Json.decodeFromString<Run>(it)
        } catch (e: Exception) {
            error(it)
        }
    }
}
