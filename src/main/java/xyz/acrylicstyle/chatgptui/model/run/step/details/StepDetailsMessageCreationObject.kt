package xyz.acrylicstyle.chatgptui.model.run.step.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StepDetailsMessageCreationObject(
    @SerialName("message_id")
    val messageId: String,
)
