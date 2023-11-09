package xyz.acrylicstyle.chatgptui.api.chat

import io.ktor.client.*
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import xyz.acrylicstyle.chatgptui.api.OpenAI
import xyz.acrylicstyle.chatgptui.model.ContentWithRole
import xyz.acrylicstyle.chatgptui.model.JsonConvertible
import xyz.acrylicstyle.chatgptui.model.JsonConvertible.Companion.encodeToString
import xyz.acrylicstyle.chatgptui.model.stream.StreamResponse
import xyz.acrylicstyle.chatgptui.util.HttpUtil

class ChatRequest(private val openAI: OpenAI, private val httpClient: HttpClient) {
    fun createStream(model: String, messages: List<ContentWithRole>, maxTokens: Int? = null, temperature: Double = 1.0, user: String? = null) =
        HttpUtil.createPostEventsFlow(
            "${System.getenv("BASE_URL") ?: "https://api.openai.com/v1"}/chat/completions",
            mapOf(
                "model" to model,
                "max_tokens" to maxTokens,
                "temperature" to temperature,
                "messages" to messages.map { it.toJson() },
                "stream" to true,
                "user" to user,
            ).encodeToString(),
            mapOf(
                "Authorization" to "Bearer ${openAI.apiKey}",
                "Content-Type" to "application/json",
            ),
        ).map { data ->
            if (data.data == "[DONE]") return@map null
            JsonConvertible.json.decodeFromString<StreamResponse>(data.data)
        }
}
