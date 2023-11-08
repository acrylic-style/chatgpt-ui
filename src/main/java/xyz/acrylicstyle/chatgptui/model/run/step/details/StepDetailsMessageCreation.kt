package xyz.acrylicstyle.chatgptui.model.run.step.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("message_creation")
data class StepDetailsMessageCreation(
    @SerialName("message_creation")
    val messageCreation: StepDetailsMessageCreationObject,
) : RunStepDetails
