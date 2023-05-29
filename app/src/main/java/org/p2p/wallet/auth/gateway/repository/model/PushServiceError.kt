package org.p2p.wallet.auth.gateway.repository.model

sealed class PushServiceError(message: String, val code: Int) : Throwable(message = "$message;code=$code") {

    class RequestCreationFailure(
        message: String,
        override val cause: Throwable?
    ) : PushServiceError(message, -1)

    class TemporaryFailure(
        code: Int,
        message: String
    ) : PushServiceError(message, code)

    class PhoneNumberAlreadyConfirmed(
        code: Int,
        message: String
    ) : PushServiceError(message, code)

    class CriticalServiceFailure(
        code: Int,
        message: String
    ) : PushServiceError(message, code)

    class TooManyRequests(
        code: Int,
        message: String,
        val cooldownTtl: Long = 0
    ) : PushServiceError(message, code)

    class SmsDeliverFailed(
        code: Int,
        message: String
    ) : PushServiceError(message, code)

    class CallDeliverFailed(
        code: Int,
        message: String
    ) : PushServiceError(message, code)

    class SolanaPublicKeyAlreadyExists(
        code: Int,
        message: String
    ) : PushServiceError(message, code)

    class UserAlreadyExists(
        code: Int,
        message: String
    ) : PushServiceError(message, code)

    class PhoneNumberNotExists(
        code: Int,
        message: String
    ) : PushServiceError(message, code)

    class IncorrectOtpCode(
        code: Int,
        message: String,
    ) : PushServiceError(message, code)

    class UnknownFailure(
        code: Int,
        message: String,
    ) : PushServiceError(message, code)

    class TooManyOtpRequests(
        code: Int,
        message: String,
        val cooldownTtl: Long = 0
    ) : PushServiceError(message, code)
}
