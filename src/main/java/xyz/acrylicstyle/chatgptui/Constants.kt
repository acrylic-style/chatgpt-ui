package xyz.acrylicstyle.chatgptui

import io.ktor.http.*

val DEV_MODE = System.getenv("DEV").toBoolean()
val staticRoute = mapOf(
    "/" to (getCacheableStaticResource("index.html") to ContentType.Text.Html.withParameter("charset", "utf-8")),
    "/main.js" to (getCacheableStaticResource("main.js") to null),
    "/image" to (getCacheableStaticResource("image.html") to ContentType.Text.Html.withParameter("charset", "utf-8")),
    "/image.js" to (getCacheableStaticResource("image.js") to null),
    "/main.css" to (getCacheableStaticResource("main.css") to null),
)
val openaiToken = System.getenv("OPENAI_TOKEN") ?: error("OPENAI_TOKEN is not defined")
val models = mapOf(
    "gpt-3.5-turbo" to "GPT-3.5 Turbo (4k context)",
    "gpt-3.5-turbo-16k" to "GPT-3.5 Turbo (16k context)",
    "gpt-4" to "GPT-4 (8k context)",
    "gpt-4-1106-preview" to "GPT-4 Turbo (128k context)",
    "gpt-4-vision-preview" to "GPT-4V (128k context)",
)
