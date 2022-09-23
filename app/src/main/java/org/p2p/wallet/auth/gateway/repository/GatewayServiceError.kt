package org.p2p.wallet.auth.gateway.repository

sealed class GatewayServiceError : Throwable() {
    abstract val code: Int

    class RequestCreationFailure(
        override val code: Int = -1,
        override val message: String,
        override val cause: Throwable?
    ) : GatewayServiceError()

    class TemporaryFailure(
        override val code: Int,
        override val message: String
    ) : GatewayServiceError()

    class PhoneNumberAlreadyConfirmed(
        override val code: Int,
        override val message: String
    ) : GatewayServiceError()

    class CriticalServiceFailure(
        override val code: Int,
        override val message: String
    ) : GatewayServiceError()

    class TooManyRequests(
        override val code: Int,
        override val message: String,
        val cooldownTtl: Long
    ) : GatewayServiceError()

    class SmsDeliverFailed(
        override val code: Int,
        override val message: String
    ) : GatewayServiceError()

    class CallDeliverFailed(
        override val code: Int,
        override val message: String
    ) : GatewayServiceError()

    class SolanaPublicKeyAlreadyExists(
        override val code: Int,
        override val message: String
    ) : GatewayServiceError()

    class UserAlreadyExists(
        override val code: Int,
        override val message: String
    ) : GatewayServiceError()

    class PhoneNumberNotExists(
        override val code: Int,
        override val message: String
    ) : GatewayServiceError()

    class IncorrectOtpCode(
        override val code: Int,
        override val message: String
    ) : GatewayServiceError()

    class UnknownFailure(
        override val code: Int,
        override val message: String
    ) : GatewayServiceError()

    class TooManyOtpRequests(
        override val code: Int,
        override val message: String,
        val cooldownTtl: Long
    ) : GatewayServiceError()
}
