package xyz.acrylicstyle.chatgptui.model.run

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.assistant.Assistant
import xyz.acrylicstyle.chatgptui.model.file.File
import xyz.acrylicstyle.chatgptui.model.run.requiredaction.RequiredAction
import xyz.acrylicstyle.chatgptui.model.assistant.tool.AssistantTool
import xyz.acrylicstyle.chatgptui.model.thread.ThreadObject

/**
 * Represents an execution run on a [ThreadObject]
 * @param id The identifier, which can be referenced in API endpoints.
 * @param object The object type, which is always `assistant.run`.
 * @param createdAt The Unix timestamp (in seconds) for when the run was created.
 * @param threadId The ID of the [ThreadObject] that was executed on as a part of this run.
 * @param assistantId The ID of the [Assistant] used for execution of this run.
 * @param status The status of the run, which can be either `queued`, `in_progress`, `requires_action`, `cancelling`,
 * `cancelled`, `failed`, `completed`, or `expired`.
 * @param requiredAction Details on the action required to continue the run. Will be `null` if no action is required.
 * @param lastError The last error associated with this run. Will be `null` if there are no errors.
 * @param expiresAt The Unix timestamp (in seconds) for when the run will expire.
 * @param startedAt The Unix timestamp (in seconds) for when the run was started.
 * @param cancelledAt The Unix timestamp (in seconds) for when the run was cancelled.
 * @param failedAt The Unix timestamp (in seconds) for when the run failed.
 * @param completedAt The Unix timestamp (in seconds) for when the run was completed.
 * @param model The model that the assistant used for this run.
 * @param instructions The instructions that the assistant used for this run.
 * @param tools The list of tools that the assistant used for this run.
 * @param fileIds The list of [File] IDs the assistant used for this run.
 * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
 * additional information about the object in a structured format. Keys can be a maximum of 64 characters long
 * and values can be a maximum of 512 characters long.
 */
@Serializable
data class Run(
    val id: String,
    val `object`: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("thread_id")
    val threadId: String,
    @SerialName("assistant_id")
    val assistantId: String,
    val status: String,
    @SerialName("required_action")
    val requiredAction: RequiredAction? = null,
    @SerialName("last_error")
    val lastError: RunError? = null,
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    @SerialName("started_at")
    val startedAt: Long? = null,
    @SerialName("cancelled_at")
    val cancelledAt: Long? = null,
    @SerialName("failed_at")
    val failedAt: Long? = null,
    @SerialName("completed_at")
    val completedAt: Long? = null,
    val model: String,
    val instructions: String? = null,
    val tools: List<AssistantTool> = emptyList(),
    @SerialName("file_ids")
    val fileIds: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
) {
    fun getRunStatus() = RunStatus.fromValue(status)
}
