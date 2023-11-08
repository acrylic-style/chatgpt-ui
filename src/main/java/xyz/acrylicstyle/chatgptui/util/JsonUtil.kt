package xyz.acrylicstyle.chatgptui.util

import io.ktor.server.application.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import xyz.acrylicstyle.chatgptui.model.JsonConvertible
import kotlin.reflect.cast

object JsonUtil {
    val prettyJson = Json {
        prettyPrint = true
        serializersModule = JsonConvertible.json.serializersModule
    }

    fun ApplicationCall.getJson() =
        if (request.queryParameters.contains("pretty")) {
            prettyJson
        } else {
            JsonConvertible.json
        }
}

// hacks (workaround) for kotlinx.serialization messing with map of <String, Any?>
// https://github.com/Kotlin/kotlinx.serialization/issues/296#issuecomment-1132714147
// (related: https://youtrack.jetbrains.com/issue/KTOR-3063)

fun Collection<*>.toJsonElement(): JsonElement = JsonArray(mapNotNull { it.toJsonElement() })

fun Map<*, *>.toJsonElement(): JsonElement = JsonObject(
    mapNotNull {
        (it.key as? String ?: return@mapNotNull null) to it.value.toJsonElement()
    }.toMap(),
)

@Suppress("UNCHECKED_CAST")
@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> T?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Map<*, *> -> toJsonElement()
    is Collection<*> -> toJsonElement()
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Enum<*> -> JsonPrimitive(this.toString())
    is JsonConvertible -> this.toJson()
    //else -> throw IllegalStateException("Can't serialize unknown type: $this (${this::class.java.typeName})")
    else -> {
        println("Serializing unknown type: $this (${this::class.java.typeName} / ${T::class.java.typeName})")
        ClassUtil.getSuperclassesAndInterfaces(this::class.java).reversed().firstNotNullOfOrNull { clazz ->
            try {
                JsonConvertible.json.encodeToJsonElement(clazz.kotlin.serializer() as SerializationStrategy<Any>, this)
            } catch (e: Exception) {
                null
            }
        }?.let { return it }
        JsonConvertible.json.encodeToJsonElement(this::class.serializer() as SerializationStrategy<T>, this)
    }
}
