package org.p2p.wallet.auth.gateway.repository

import org.p2p.wallet.auth.gateway.api.response.GatewayServiceErrorResponse
import timber.log.Timber

class GatewayServiceErrorMapper {
    fun fromNetwork(error: GatewayServiceErrorResponse): GatewayServiceError =
        when (error.errorCode) {
            -32050 -> GatewayServiceError.TemporaryFailure(
                error.errorCode, error.errorMessage
            )
            -32051 -> GatewayServiceError.PhoneNumberAlreadyConfirmed(
                error.errorCode, error.errorMessage
            )
            -32052, -32058, -32700, -32600, -32601, -32602, -32603 -> GatewayServiceError.CriticalServiceFailure(
                error.errorCode, error.errorMessage
            )
            // PWN-4774 - This error temporary unavailable
            -32053 -> GatewayServiceError.TooManyRequests(
                error.errorCode, error.errorMessage
            )
            -32054 -> GatewayServiceError.SmsDeliverFailed(
                error.errorCode, error.errorMessage
            )
            -32055 -> GatewayServiceError.CallDeliverFailed(
                error.errorCode, error.errorMessage
            )
            -32056 -> GatewayServiceError.SolanaPublicKeyAlreadyExists(
                error.errorCode, error.errorMessage
            )
            -32057 -> GatewayServiceError.UserAlreadyExists(
                error.errorCode, error.errorMessage
            )
            -32059 -> GatewayServiceError.TooOftenOtpRequests(
                error.errorCode, error.errorMessage
            )
            -32060 -> GatewayServiceError.PhoneNumberNotExists(
                error.errorCode, error.errorMessage
            )
            -32061 -> GatewayServiceError.IncorrectOtpCode(
                error.errorCode, error.errorMessage
            )
            else -> {
                val unknownCodeError = GatewayServiceError.UnknownFailure(error.errorCode, error.errorMessage)
                Timber.tag("GatewayServiceMapper").e(unknownCodeError)
                unknownCodeError
            }
        }
}
