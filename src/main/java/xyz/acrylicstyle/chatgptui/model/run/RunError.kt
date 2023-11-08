package xyz.acrylicstyle.chatgptui.model.run

import kotlinx.serialization.Serializable

/**
 * @param code One of `server_error` or `rate_limit_exceeded`.
 * @param message A human-readable description of the error.
 */
@Serializable
data class RunError(val code: String, val message: String)
