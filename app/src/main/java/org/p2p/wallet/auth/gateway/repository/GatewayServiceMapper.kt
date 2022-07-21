package org.p2p.wallet.auth.gateway.repository

import org.near.borshj.Borsh
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.solanaj.utils.crypto.decodeFromBase58
import org.p2p.solanaj.utils.crypto.encodeToBase58
import org.p2p.wallet.auth.gateway.api.request.RegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceErrorResponse
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceResponse
import org.p2p.wallet.utils.Constants
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PATTERN_GATEWAY_SERVICE = "yyyy-MM-dd HH:mm:ss.n x"

class GatewayServiceMapper {
    private class RegisterWalletSignatureStruct(
        val clientId: String,
        val etheriumId: String,
        val phone: String,
        val channelType: String
    ) : Borsh

    @Throws(GatewayServiceError::class)
    fun <T> fromNetwork(response: GatewayServiceResponse<T>): T {
        if (response.errorBody != null) {
            throw gatewayServiceError(response.errorBody)
        }
        return response.result!!
    }

    private fun gatewayServiceError(error: GatewayServiceErrorResponse): GatewayServiceError =
        when (error.errorCode) {
            -32050 -> GatewayServiceError.TemporaryFailure(error.errorCode, error.errorMessage)
            -32051 -> GatewayServiceError.PhoneNumberAlreadyConfirmed(error.errorCode, error.errorMessage)
            -32052 -> GatewayServiceError.CriticalServiceFailure(error.errorCode, error.errorMessage)
            -32053 -> GatewayServiceError.TooManyRequests(error.errorCode, error.errorMessage)
            -32054 -> GatewayServiceError.SmsDeliverFailed(error.errorCode, error.errorMessage)
            -32055 -> GatewayServiceError.CallDeliverFailed(error.errorCode, error.errorMessage)
            -32056 -> GatewayServiceError.SolanaPublicKeyAlreadyExists(error.errorCode, error.errorMessage)
            -32057 -> GatewayServiceError.UserAlreadyExists(error.errorCode, error.errorMessage)
            -32060 -> GatewayServiceError.PhoneNumberNotExists(error.errorCode, error.errorMessage)
            else -> {
                val unknownCodeError = GatewayServiceError.UnknownFailure(error.errorCode, error.errorMessage)
                Timber.tag("GatewayServiceMapper").e(unknownCodeError)
                unknownCodeError
            }
        }

    fun toNetwork(
        userPublicKey: String,
        userPrivateKey: String,
        etheriumPublicKey: String,
        phoneNumber: String,
        channel: RegisterWalletRequest.OtpMethod
    ): RegisterWalletRequest {
        return RegisterWalletRequest.Params(
            clientPublicKeyB58 = userPublicKey,
            etheriumPublicKeyB58 = etheriumPublicKey,
            userPhone = phoneNumber,
            appHash = Constants.APP_HASH,
            channel = channel,
            requestSignature = createSignatureField(
                userPublicKey = userPublicKey,
                solanaPrivateKey = userPrivateKey,
                etheriumPublicKey = etheriumPublicKey,
                phoneNumber = phoneNumber,
                channel = channel.backendName
            ),
            timestamp = createTimestampField(),
        )
            .let { RegisterWalletRequest(it) }
    }

    @Throws(GatewayServiceError.RequestCreationFailure::class)
    private fun createSignatureField(
        userPublicKey: String,
        solanaPrivateKey: String,
        etheriumPublicKey: String,
        phoneNumber: String,
        channel: String
    ): String {
        try {
            val signatureStructBytes = Borsh.serialize(
                RegisterWalletSignatureStruct(userPublicKey, etheriumPublicKey.lowercase(), phoneNumber, channel)
            )
            val userPublicKeyBytes = userPublicKey.decodeFromBase58()
            val solanaPrivateKeyBytes = solanaPrivateKey.decodeFromBase58()
            return TweetNaclFast.Signature(userPublicKeyBytes.copyOf(), solanaPrivateKeyBytes.copyOf())
                .sign(signatureStructBytes)
                .encodeToBase58()
        } catch (error: Throwable) {
            Timber.e(error)
            throw GatewayServiceError.RequestCreationFailure(
                message = "Failed to generate signature field",
                cause = error
            )
        }
    }

    private fun createTimestampField(): String {
        return SimpleDateFormat(PATTERN_GATEWAY_SERVICE, Locale.getDefault()).format(Date())
    }
}
