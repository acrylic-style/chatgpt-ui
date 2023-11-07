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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import xyz.acrylicstyle.chatgptui.model.EventData
import xyz.acrylicstyle.chatgptui.model.request.ChatRequest
import xyz.acrylicstyle.chatgptui.model.request.CreateImageRequest
import xyz.acrylicstyle.chatgptui.model.stream.StreamResponse
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

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

suspend inline fun <reified T> ApplicationCall.respondJson(value: T, status: HttpStatusCode = HttpStatusCode.OK) {
    respondText(Json.encodeToString(value), ContentType.Application.Json, status)
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

fun createPostEventsFlow(url: String, body: String, headers: Map<String, String> = emptyMap()): Flow<EventData> =
    flow {
        val conn = (URL(url).openConnection() as HttpURLConnection).also {
            headers.forEach { (key, value) -> it.setRequestProperty(key, value) }
            it.setRequestProperty("Accept", "text/event-stream")
            it.doInput = true
            it.doOutput = true
        }

        conn.connect()

        conn.outputStream.write(body.toByteArray())

        if (conn.responseCode !in 200..399) {
            error("Request failed with ${conn.responseCode}: ${conn.errorStream.bufferedReader().readText()}")
        }

        val reader = conn.inputStream.bufferedReader()

        var event = EventData()

        while (true) {
            val line = reader.readLine() ?: break

            when {
                line.startsWith("event:") -> event = event.copy(name = line.substring(6).trim())
                line.startsWith("data:") -> event = event.copy(data = line.substring(5).trim())
                line.isEmpty() -> {
                    emit(event)
                    event = EventData()
                }
            }
        }
    }.flowOn(Dispatchers.IO)

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

            val body = ChatRequest(
                data.model,
                if ("vision" in data.model) 2000 else null,
                1.0,
                data.content,
                true,
                call.request.header("CF-Connecting-IP") ?: call.request.host(),
            )
            call.response.header("X-Accel-Buffering", "no")
            call.response.header(HttpHeaders.CacheControl, "no-cache")
            call.response.header(HttpHeaders.ContentType, "text/event-stream")
            call.respondTextWriter {
                createPostEventsFlow(
                    "${System.getenv("BASE_URL") ?: "https://api.openai.com/v1"}/chat/completions",
                    Json.encodeToString(body),
                    mapOf(
                        "Authorization" to "Bearer $openaiToken",
                        "Content-Type" to "application/json",
                    ),
                ).collect { data ->
                    withContext(Dispatchers.IO) {
                        try {
                            if (data.data == "[DONE]") {
                                this@respondTextWriter.close()
                            } else {
                                val response = json.decodeFromString<StreamResponse>(data.data)
                                this@respondTextWriter.write(response.choices[0].delta.content)
                                this@respondTextWriter.flush()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
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
    }
}
