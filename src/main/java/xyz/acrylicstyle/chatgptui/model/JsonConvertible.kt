package xyz.acrylicstyle.chatgptui.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import xyz.acrylicstyle.chatgptui.api.threads.ThreadCreateBody
import xyz.acrylicstyle.chatgptui.serializers.DynamicLookupSerializer
import xyz.acrylicstyle.chatgptui.util.toJsonElement

interface JsonConvertible {
    fun toJson(): JsonElement

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val json = Json {
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                contextual(ThreadCreateBody.ThreadInitialMessage::class, ThreadCreateBody.ThreadInitialMessage.serializer())
                contextual(Any::class, DynamicLookupSerializer)
            }
        }

        inline fun <reified T> T.encodeToString(json: Json = JsonConvertible.json): String =
            try {
                json.encodeToString(if (this is JsonConvertible) this.toJson() else this.toJsonElement())
            } catch (e: Exception) {
                json.encodeToString(this)
            }
    }
}
