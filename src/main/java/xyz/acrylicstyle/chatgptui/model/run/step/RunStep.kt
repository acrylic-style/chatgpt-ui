package xyz.acrylicstyle.chatgptui.model.run.step

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.acrylicstyle.chatgptui.model.assistant.Assistant
import xyz.acrylicstyle.chatgptui.model.run.Run
import xyz.acrylicstyle.chatgptui.model.run.RunError
import xyz.acrylicstyle.chatgptui.model.run.step.details.RunStepDetails
import xyz.acrylicstyle.chatgptui.model.thread.ThreadObject

/**
 * Represents a step in execution of a run.
 * @param id The identifier of the run step, which can be referenced in API endpoints.
 * @param object The object type, which is always `assistant.run.step`.
 * @param createdAt The Unix timestamp (in seconds) for when the run step was created.
 * @param assistantId The ID of the [Assistant] associated with the run step.
 * @param threadId The ID of the [ThreadObject] that was run.
 * @param runId The ID of the [Run] that this run step is a part of.
 * @param type The type of run step, which can be either `message_creation` or `tool_calls`.
 * @param status The status of the run, which can be either `in_progress`, `cancelled`, `failed`, `completed`, or `expired`.
 * @param stepDetails The details of the run step.
 * @param lastError The last error associated with this run step. Will be `null` if there are no errors.
 * @param expiresAt The Unix timestamp (in seconds) for when the run step expired. A step is considered expired if the
 * parent run is expired.
 * @param cancelledAt The Unix timestamp (in seconds) for when the run step was cancelled.
 * @param failedAt The Unix timestamp (in seconds) for when the run step failed.
 * @param completedAt The Unix timestamp (in seconds) for when the run step completed.
 * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
 * additional information about the object in a structured format. Keys can be a maximum of 64 characters long and
 * values can be a maximum of 512 characters long.
 */
@Serializable
data class RunStep(
    val id: String,
    val `object`: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("assistant_id")
    val assistantId: String,
    @SerialName("thread_id")
    val threadId: String,
    @SerialName("run_id")
    val runId: String,
    val type: String,
    val status: String,
    @SerialName("step_details")
    val stepDetails: RunStepDetails,
    @SerialName("last_error")
    val lastError: RunError? = null,
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    @SerialName("cancelled_at")
    val cancelledAt: Long? = null,
    @SerialName("failed_at")
    val failedAt: Long? = null,
    @SerialName("completed_at")
    val completedAt: Long? = null,
    val metadata: Map<String, String> = emptyMap(),
)
