@file:JvmName("MainKt")

package xyz.acrylicstyle.chatgptui

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import xyz.acrylicstyle.chatgptui.api.OpenAI
import xyz.acrylicstyle.chatgptui.model.ContentWithRole
import xyz.acrylicstyle.chatgptui.api.threads.ThreadCreateBody
import xyz.acrylicstyle.chatgptui.model.assistant.tool.AssistantTool
import xyz.acrylicstyle.chatgptui.model.request.CreateImageRequest
import xyz.acrylicstyle.chatgptui.model.run.ToolOutputs
import xyz.acrylicstyle.chatgptui.util.HttpUtil.respondJson
import java.io.InputStream

private val client = HttpClient(CIO) {
    engine {
        this.requestTimeout = 1000 * 60
    }
}

fun getStaticResource(name: String): InputStream? = Application::class.java.getResourceAsStream("/static/$name")

fun getStaticResourceAsBytes(name: String) = getStaticResource(name)?.use { it.readAllBytes() }

fun getCacheableStaticResource(name: String): () -> ByteArray =
    if (DEV_MODE) {
        ({ getStaticResourceAsBytes(name) ?: error("Unable to load $name") })
    } else {
        val byteArray = getStaticResourceAsBytes(name) ?: error("Unable to load $name")
        ({ byteArray })
    }

fun main() {
    if (DEV_MODE) println("Resource caching is disabled")
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toIntOrNull() ?: 8080,
        host = System.getenv("HOST") ?: "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

private val json = Json {
    ignoreUnknownKeys = true
}

fun Application.module() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHost("localhost:3000")
        allowHost("127.0.0.1:3000")
        System.getenv("ALLOWED_HOSTS")?.split(",")?.forEach { allowHost(it) }
    }
    install (Routing)

    val openAI = OpenAI(openaiToken, System.getenv("BASE_URL") ?: "https://api.openai.com/v1")

    routing {
        staticRoute.forEach { (path, pair) ->
            get(path) {
                call.respondBytes(pair.first(), pair.second)
            }
        }

        get("/models") {
            call.respondJson(models)
        }

        post("/generate") {
            @Serializable
            data class Generate(val model: String, val content: List<JsonObject>)

            val data: Generate = json.decodeFromString(call.receiveText())

            if (data.model !in models) {
                return@post call.respondJson(mapOf("error" to "invalid model"))
            }

            call.response.header("X-Accel-Buffering", "no")
            call.response.header(HttpHeaders.CacheControl, "no-cache")
            call.response.header(HttpHeaders.ContentType, "text/event-stream")
            call.respondTextWriter {
                openAI.chat.createStream(
                    data.model,
                    data.content.map { ContentWithRole.fromJson(it) },
                    maxTokens = (if ("vision" in data.model) 2000 else null),
                    user = (call.request.header("CF-Connecting-IP") ?: call.request.host())
                ).collect { data ->
                    withContext(Dispatchers.IO) {
                        try {
                            if (data == null) {
                                this@respondTextWriter.close()
                            } else {
                                this@respondTextWriter.write(data.choices[0].delta.content)
                                this@respondTextWriter.flush()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            this@respondTextWriter.close()
                        }
                    }
                }
            }
        }

        post("/generate_image") {
            @Serializable
            data class Generate(val prompt: String, val count: Int, val size: String)

            val data: Generate = Json.decodeFromString(call.receiveText())
            val response = client.post("https://api.openai.com/v1/images/generations") {
                setBody(
                    Json.encodeToString(
                        CreateImageRequest(
                            data.prompt,
                            data.count,
                            data.size,
                            "b64_json",
                            call.request.host(),
                        )
                    )
                )
                header("Authorization", "Bearer $openaiToken")
                header("Content-Type", "application/json")
            }
            call.respondText(response.bodyAsText(), ContentType.Application.Json)
        }

        post("/threads/create_and_run") {
            val data: CreateAndRunRequest = Json.decodeFromString(call.receiveText())
            if (data.messages.size != 1) error("Must have exact 1 messages")
            val uploadedFiles = mutableListOf<String>()
            try {
                data.files.forEach {
                    uploadedFiles += openAI.files.upload(it.name, it.data, "assistants").id
                }
                openAI.threads.createAndRun(
                    System.getenv("ASSISTANT_ID") ?: error("ASSISTANT_ID is not set"),
                    listOf(data.messages[0].copy(fileIds = uploadedFiles)),
                    model = data.model,
                    instructions = data.instructions,
                    tools = data.tools,
                ).let { call.respondJson(it) }
            } catch (e: Exception) {
                uploadedFiles.forEach {
                    try {
                        openAI.files.delete(it)
                    } catch (e: Exception) {
                        System.err.println("Error deleting file $it")
                        e.printStackTrace()
                    }
                }
                throw e
            }
        }

        delete("/threads/{thread_id}") {
            val threadId = call.parameters["thread_id"] ?: error("thread_id is not specified")
            try {
                val messages = openAI.threads.messages(threadId).list(100)
                var deleted = 0
                messages.data.forEach { message ->
                    message.fileIds.forEach { file ->
                        try {
                            openAI.files.delete(file)
                            deleted++
                        } catch (e: Exception) {
                            System.err.println("Failed to delete file $file")
                            e.printStackTrace()
                        }
                    }
                }
                println("Deleted $deleted files from $threadId")
            } catch (e: Exception) {
                System.err.println("Failed to fetch messages")
                e.printStackTrace()
            }
            openAI.threads.delete(threadId).let { call.respondJson(it) }
        }

        get("/threads/{thread_id}/messages") {
            val threadId = call.parameters["thread_id"] ?: error("thread_id is not specified")
            openAI.threads
                .messages(threadId)
                .list(10)
                .let { call.respondJson(it) }
        }

        post("/threads/{thread_id}/messages") {
            @Serializable
            data class RequestBody(val role: String, val content: String, val fileIds: List<String> = emptyList())

            val threadId = call.parameters["thread_id"] ?: error("thread_id is not specified")
            val body = json.decodeFromString<RequestBody>(call.receiveText())
            openAI.threads
                .messages(threadId)
                .create(body.role, body.content, body.fileIds)
                .let { call.respondJson(it) }
        }

        post("/threads/{thread_id}/runs") {
            val threadId = call.parameters["thread_id"] ?: error("thread_id is not specified")
            val data = json.decodeFromString<CreateRunRequest>(call.receiveText())
            openAI.threads
                .runs(threadId)
                .create(
                    System.getenv("ASSISTANT_ID") ?: error("ASSISTANT_ID is not set"),
                    data.model,
                    data.instructions,
                    data.tools,
                )
                .let { call.respondJson(it) }
        }

        get("/threads/{thread_id}/runs/{run_id}") {
            val threadId = call.parameters["thread_id"] ?: error("thread_id is not specified")
            val runId = call.parameters["run_id"] ?: error("run_id is not specified")
            openAI.threads
                .runs(threadId)
                .get(runId)
                .let { call.respondJson(it) }
        }

        post("/threads/{thread_id}/runs/{run_id}/submit_tool_outputs") {
            val threadId = call.parameters["thread_id"] ?: error("thread_id is not specified")
            val runId = call.parameters["run_id"] ?: error("run_id is not specified")
            val data = json.decodeFromString<List<ToolOutputs>>(call.receiveText())
            openAI.threads
                .runs(threadId)
                .submitToolOutputs(runId, data)
                .let { call.respondJson(it) }
        }

        post("/request") {
            client.get(call.receiveText()) {
                header("User-Agent", "ktor client")
            }.let { call.respondBytes(it.bodyAsChannel().toByteArray(), contentType = it.contentType()) }
        }

        post("/search") {
            val key = System.getenv("CUSTOM_SEARCH_KEY")
                ?: return@post call.respondJson(mapOf("error" to "Search API is unavailable"))
            val id = System.getenv("CUSTOM_SEARCH_ID")
                ?: return@post call.respondJson(mapOf("error" to "Search API is unavailable"))
            val query = call.receiveText()
            client.get("https://customsearch.googleapis.com/customsearch/v1") {
                header("User-Agent", "ktor client")
                parameter("key", key)
                parameter("cx", id)
                parameter("q", query)
                parameter("gl", call.request.header("CF-IPCountry")?.lowercase())
            }.let { call.respondText(it.bodyAsText(), contentType = it.contentType()) }
        }

        get("/threads/{thread_id}/runs/{run_id}/steps") {
            val threadId = call.parameters["thread_id"] ?: error("thread_id is not specified")
            val runId = call.parameters["run_id"] ?: error("run_id is not specified")
            openAI.threads
                .runs(threadId)
                .steps(runId)
                .list()
                .let { call.respondJson(it) }
        }

        get("/files/{id}") {
            call.respondJson(openAI.files.get(call.parameters["id"]!!))
        }

        get("/files/{id}/content") {
            call.respondBytes(openAI.files.getContent(call.parameters["id"]!!))
        }
    }
}

@Serializable
data class CreateRunRequest(
    val model: String,
    val instructions: String?,
    val tools: List<AssistantTool> = emptyList()
)

@Serializable
data class CreateAndRunRequest(
    val model: String,
    val instructions: String?,
    val tools: List<AssistantTool> = emptyList(),
    val messages: List<ThreadCreateBody.ThreadInitialMessage>,
    val files: List<FileData> = emptyList(),
)

@Serializable
class FileData(val name: String, val data: ByteArray)
