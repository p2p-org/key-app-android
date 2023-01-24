package org.p2p.wallet.auth.gateway.repository.model

sealed class GatewayServiceError(message: String, val code: Int) : Throwable(message = "$message;code=$code") {

    class RequestCreationFailure(
        message: String,
        override val cause: Throwable?
    ) : GatewayServiceError(message, -1)

    class TemporaryFailure(
        code: Int,
        message: String
    ) : GatewayServiceError(message, code)

    class PhoneNumberAlreadyConfirmed(
        code: Int,
        message: String
    ) : GatewayServiceError(message, code)

    class CriticalServiceFailure(
        code: Int,
        message: String
    ) : GatewayServiceError(message, code)

    class TooManyRequests(
        code: Int,
        message: String,
        val cooldownTtl: Long
    ) : GatewayServiceError(message, code)

    class SmsDeliverFailed(
        code: Int,
        message: String
    ) : GatewayServiceError(message, code)

    class CallDeliverFailed(
        code: Int,
        message: String
    ) : GatewayServiceError(message, code)

    class SolanaPublicKeyAlreadyExists(
        code: Int,
        message: String
    ) : GatewayServiceError(message, code)

    class UserAlreadyExists(
        code: Int,
        message: String
    ) : GatewayServiceError(message, code)

    class PhoneNumberNotExists(
        code: Int,
        message: String
    ) : GatewayServiceError(message, code)

    class IncorrectOtpCode(
        code: Int,
        message: String,
    ) : GatewayServiceError(message, code)

    class UnknownFailure(
        code: Int,
        message: String,
    ) : GatewayServiceError(message, code)

    class TooManyOtpRequests(
        code: Int,
        message: String,
        val cooldownTtl: Long
    ) : GatewayServiceError(message, code)
}
