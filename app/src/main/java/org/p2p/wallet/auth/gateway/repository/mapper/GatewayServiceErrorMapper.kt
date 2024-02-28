package org.p2p.wallet.auth.gateway.repository.mapper

import org.p2p.wallet.auth.gateway.api.response.GatewayServiceErrorResponse
import org.p2p.wallet.auth.gateway.repository.model.PushServiceError
import timber.log.Timber

class GatewayServiceErrorMapper {
    fun fromNetwork(error: GatewayServiceErrorResponse): PushServiceError =
        when (error.errorCode) {
            -32050 -> PushServiceError.TemporaryFailure(
                error.errorCode, error.errorMessage
            )
            -32051 -> PushServiceError.PhoneNumberAlreadyConfirmed(
                error.errorCode, error.errorMessage
            )
            -32052, -32058, -32700, -32600, -32601, -32602, -32603 -> PushServiceError.CriticalServiceFailure(
                error.errorCode, error.errorMessage
            )
            // PWN-4774 - This error temporary unavailable
            -32053 -> PushServiceError.TooManyRequests(
                error.errorCode, error.errorMessage, cooldownTtl = error.data?.cooldownTtl ?: 0L
            )
            -32054 -> PushServiceError.SmsDeliverFailed(
                error.errorCode, error.errorMessage
            )
            -32055 -> PushServiceError.CallDeliverFailed(
                error.errorCode, error.errorMessage
            )
            -32056 -> PushServiceError.SolanaPublicKeyAlreadyExists(
                error.errorCode, error.errorMessage
            )
            -32057 -> PushServiceError.UserAlreadyExists(
                error.errorCode, error.errorMessage
            )
            -32059 -> PushServiceError.TooManyOtpRequests(
                error.errorCode, error.errorMessage, error.data?.cooldownTtl ?: 0L
            )
            -32060 -> PushServiceError.PhoneNumberNotExists(
                error.errorCode, error.errorMessage
            )
            -32061 -> PushServiceError.IncorrectOtpCode(
                error.errorCode, error.errorMessage
            )
            else -> {
                val unknownCodeError = PushServiceError.UnknownFailure(error.errorCode, error.errorMessage)
                Timber.tag("GatewayServiceMapper").e(unknownCodeError)
                unknownCodeError
            }
        }
            .also { Timber.tag("GatewayServiceMapper").i(error.toString()) }
}
