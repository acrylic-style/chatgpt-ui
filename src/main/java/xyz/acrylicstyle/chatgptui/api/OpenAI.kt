package xyz.acrylicstyle.chatgptui.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import xyz.acrylicstyle.chatgptui.api.assistants.AssistantsRequest
import xyz.acrylicstyle.chatgptui.api.chat.ChatRequest
import xyz.acrylicstyle.chatgptui.api.files.FilesRequest
import xyz.acrylicstyle.chatgptui.api.threads.ThreadsRequest

class OpenAI(val apiKey: String, val baseUrl: String = "https://api.openai.com/v1") {
    private val client = HttpClient(CIO) {
        engine {
            this.requestTimeout = 1000 * 60 * 5
        }
    }

    val assistants = AssistantsRequest(this, client)
    val chat = ChatRequest(this, client)
    val files = FilesRequest(this, client)
    val threads = ThreadsRequest(this, client)
}
