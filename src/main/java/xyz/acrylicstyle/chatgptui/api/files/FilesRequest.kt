package xyz.acrylicstyle.chatgptui.api.files

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import xyz.acrylicstyle.chatgptui.api.OpenAI
import xyz.acrylicstyle.chatgptui.model.DeletionStatus
import xyz.acrylicstyle.chatgptui.model.JsonConvertible.Companion.encodeToString
import xyz.acrylicstyle.chatgptui.model.ListResult
import xyz.acrylicstyle.chatgptui.model.file.File
import java.util.*

class FilesRequest(private val openAI: OpenAI, private val httpClient: HttpClient) {
    /**
     * Returns a list of files that belong to the user's organization.
     * @param purpose Only return files with the given purpose.
     * @return A list of [File] objects.
     */
    suspend fun list(purpose: String? = null) =
        httpClient.get("${openAI.baseUrl}/files") {
            header("Authorization", "Bearer ${openAI.apiKey}")
            parameter("purpose", purpose)
        }.bodyAsText().let { Json.decodeFromString<ListResult<File>>(it) }

    /**
     * Upload a file that can be used across various endpoints/features.
     * The size of all the files uploaded by one organization can be up to 100 GB.
     *
     * The size of individual files for can be a maximum of 512MB.
     * See the [Assistants Tools guide](https://platform.openai.com/docs/assistants/tools) to learn more about the
     * types of files supported. The Fine-tuning API only supports `.jsonl` files.
     *
     * You should contact OpenAI if you need to increase these storage limits.
     *
     * @param file The File object (not file name) to be uploaded.
     * @param purpose The intended purpose of the uploaded file.
     * Use "fine-tune" for [Fine-tuning](https://platform.openai.com/docs/api-reference/fine-tuning) and "assistants"
     * for [Assistants](https://platform.openai.com/docs/api-reference/assistants) and [Messages](https://platform.openai.com/docs/api-reference/messages).
     * This allows OpenAI to validate the format of the uploaded file is correct for fine-tuning.
     * @return The uploaded [File] object.
     */
    suspend fun upload(filename: String, file: ByteArray, purpose: String) =
        httpClient.post("${openAI.baseUrl}/files") {
            header("Authorization", "Bearer ${openAI.apiKey}")
//            header("Content-Type", "application/json")
            val boundary = "-".repeat(16) + UUID.randomUUID().toString()
            setBody(MultiPartFormDataContent(formData {
                append("file", file, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"${filename}\"")
                })
                append("purpose", purpose)
            }, boundary, ContentType.MultiPart.FormData.withParameter("boundary", boundary)))
//            setBody(mapOf(
//                "purpose" to purpose,
//                "file" to file,
//            ).encodeToString())
        }.bodyAsText().let {
            try {
                Json.decodeFromString<File>(it)
            } catch (e: Exception) {
                error("Invalid response: $it")
            }
        }

    /**
     * Delete a file.
     * @param fileId The ID of the file to use for this request.
     * @return Deletion status
     */
    suspend fun delete(fileId: String) =
        httpClient.delete("${openAI.baseUrl}/files/$fileId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
        }.bodyAsText().let { Json.decodeFromString<DeletionStatus>(it) }

    /**
     * Returns information about a specific file.
     * @param fileId The ID of the file to use for this request.
     * @return The [File] Object matching the specified ID.
     */
    suspend fun get(fileId: String) =
        httpClient.get("${openAI.baseUrl}/files/$fileId") {
            header("Authorization", "Bearer ${openAI.apiKey}")
        }.bodyAsText().let { Json.decodeFromString<File>(it) }

    /**
     * Returns the contents of the specified file.
     * @param fileId The ID of the file to use for this request.
     * @return The file content.
     */
    suspend fun getContent(fileId: String) =
        httpClient.get("${openAI.baseUrl}/files/$fileId/content") {
            header("Authorization", "Bearer ${openAI.apiKey}")
        }.bodyAsChannel().toByteArray()
}
