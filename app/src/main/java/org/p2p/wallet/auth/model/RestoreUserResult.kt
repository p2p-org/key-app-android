package org.p2p.wallet.auth.model

sealed interface RestoreUserResult {
    object RestoreSuccessful : RestoreUserResult
    class RestoreFailed(override val cause: Throwable) : Error(), RestoreUserResult
}
