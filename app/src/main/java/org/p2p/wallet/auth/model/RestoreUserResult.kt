package org.p2p.wallet.auth.model

class RestoreUserException(
    message: String,
    val errorCode: Int? = null
) : Throwable(message = message)

sealed interface RestoreUserResult {

    open class RestoreFailure(override val cause: Throwable) : Throwable(message = cause.message), RestoreUserResult {
        open class DevicePlusCustomShare(val exception: RestoreUserException) : RestoreFailure(exception) {
            object UserNotFound :
                DevicePlusCustomShare(RestoreUserException("DevicePlusCustomShare: User not found"))

            object SharesDoesNotMatch :
                DevicePlusCustomShare(RestoreUserException("DevicePlusCustomShare: Shares does not match"))
        }

        open class DevicePlusSocialShare(val exception: RestoreUserException) : RestoreFailure(exception) {
            data class DeviceAndSocialShareNotMatch(val userEmailAddress: String) :
                DevicePlusSocialShare(RestoreUserException("DevicePlusSocialShare: Device and social does not match"))

            data class SocialShareNotFound(val userEmailAddress: String) :
                DevicePlusSocialShare(RestoreUserException("DevicePlusSocialShare: Social share not found"))
        }

        open class SocialPlusCustomShare(val exception: RestoreUserException) : RestoreFailure(exception) {
            object TorusKeyNotFound :
                SocialPlusCustomShare(RestoreUserException("SocialPlusCustomShare: Torus key has not been initialized"))

            data class SocialShareNotFound(val userEmailAddress: String) :
                SocialPlusCustomShare(RestoreUserException("SocialPlusCustomShare: Social share not found"))

            data class SocialShareNotMatch(val userEmailAddress: String) :
                SocialPlusCustomShare(RestoreUserException("SocialPlusCustomShare: Social share not match"))
        }

        open class DevicePlusCustomOrSocialPlusCustom(val exception: RestoreUserException) : RestoreFailure(exception)

        open class DevicePlusSocialOrSocialPlusCustom(val exception: RestoreUserException) : RestoreFailure(exception)
    }

    open class RestoreSuccess : RestoreUserResult {
        object DevicePlusCustomShare : RestoreSuccess()

        object DevicePlusSocialShare : RestoreSuccess()

        object SocialPlusCustomShare : RestoreSuccess()
    }
}
