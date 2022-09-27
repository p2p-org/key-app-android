package org.p2p.wallet.auth.model

sealed interface RestoreUserResult {
    object RestoreSuccessful : RestoreUserResult
    object UserNotFound : RestoreUserResult
    object SharesDoesNotMatch : RestoreUserResult
    class RestoreFailed(override val cause: Throwable) : Throwable(message = cause.message), RestoreUserResult
    object DeviceShareNotFound : RestoreUserResult
}
