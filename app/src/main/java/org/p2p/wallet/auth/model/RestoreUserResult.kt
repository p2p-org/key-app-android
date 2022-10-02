package org.p2p.wallet.auth.model

sealed interface RestoreUserResult {
    object RestoreSuccessful : RestoreUserResult
    object UserNotFound : RestoreUserResult
    object SharesDoNotMatch : RestoreUserResult
    class RestoreFailed(override val cause: Throwable) : Throwable(message = cause.message), RestoreUserResult
    object DeviceShareNotFound : RestoreUserResult
    object SocialAuthRequired : RestoreUserResult
    data class DeviceAndSocialShareNotMatch(val socialShareUserId: String) : RestoreUserResult
    data class SocialShareNotFound(val socialShareUserId: String) : RestoreUserResult
}
