package xyz.acrylicstyle.chatgptui.model.run

enum class RunStatus(val value: String) {
    Queued("queued"),
    InProgress("in_progress"),
    RequiresAction("requires_action"),
    Cancelling("cancelling"),
    Cancelled("cancelled"),
    Failed("failed"),
    Completed("completed"),
    Expired("expired"),
    ;

    companion object {
        fun fromValue(value: String): RunStatus? = entries.find { it.value == value }
    }
}
