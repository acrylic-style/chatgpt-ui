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
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

val DEV_MODE = System.getenv("DEV").toBoolean()

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

val staticRoute = mapOf(
    "/" to (getCacheableStaticResource("index.html") to ContentType.Text.Html.withParameter("charset", "utf-8")),
    "/main.js" to (getCacheableStaticResource("main.js") to null),
    "/image" to (getCacheableStaticResource("image.html") to ContentType.Text.Html.withParameter("charset", "utf-8")),
    "/image.js" to (getCacheableStaticResource("image.js") to null),
    "/main.css" to (getCacheableStaticResource("main.css") to null),
)

val openaiToken = System.getenv("OPENAI_TOKEN") ?: error("OPENAI_TOKEN is not defined")

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

fun Application.module() {
    install(Routing)

    routing {
        staticRoute.forEach { (path, pair) ->
            get(path) {
                call.respondBytes(pair.first(), pair.second)
            }
        }

        post("/generate") {
            @Serializable
            data class Generate(val model: String, val content: List<ContentWithRole>)

            val data: Generate = Json.decodeFromString(call.receiveText())
            val body = CompletionsBody(
                    data.model,
                    null,
                    1.0,
                    data.content,
                    true,
                    call.request.host(),
            )
            call.response.header("X-Accel-Buffering", "no")
            call.response.header(HttpHeaders.CacheControl, "no-cache")
            call.response.header(HttpHeaders.ContentType, "text/event-stream")
            call.respondTextWriter {
                createPostEventsFlow(
                        "https://api.openai.com/v1/chat/completions",
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
                                val response = Json.decodeFromString<StreamResponse>(data.data)
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
