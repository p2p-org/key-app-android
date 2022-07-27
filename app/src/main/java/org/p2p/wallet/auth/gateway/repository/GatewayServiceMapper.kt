package org.p2p.wallet.auth.gateway.repository

import org.near.borshj.Borsh
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.solanaj.utils.crypto.decodeFromBase58
import org.p2p.solanaj.utils.crypto.encodeToBase58
import org.p2p.wallet.auth.gateway.api.request.ConfirmRegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceJsonRpcMethods
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceRequest
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.request.RegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceErrorResponse
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceResponse
import org.p2p.wallet.utils.Constants
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TIMESTAMP_PATTERN_GATEWAY_SERVICE = "yyyy-MM-dd HH:mm:ss.n x"

class GatewayServiceMapper {
    private class RegisterWalletSignatureStruct(
        val clientId: String,
        val etheriumId: String,
        val phone: String,
        val channelType: String
    ) : Borsh

    private class ConfirmRegisterWalletSignatureStruct(
        val clientId: String,
        val etheriumId: String,
        val encryptedShare: String,
        val phone: String,
        val phoneConfirmationCode: String
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
            -32061 -> GatewayServiceError.IncorrectOtpCode(error.errorCode, error.errorMessage)
            else -> {
                val unknownCodeError = GatewayServiceError.UnknownFailure(error.errorCode, error.errorMessage)
                Timber.tag("GatewayServiceMapper").e(unknownCodeError)
                unknownCodeError
            }
        }

    fun toRegisterWalletNetwork(
        userPublicKey: String,
        userPrivateKey: String,
        etheriumPublicKey: String,
        phoneNumber: String,
        channel: OtpMethod
    ): GatewayServiceRequest<RegisterWalletRequest> {
        return RegisterWalletRequest(
            clientSolanaPublicKeyB58 = userPublicKey,
            etheriumPublicKeyB58 = etheriumPublicKey,
            userPhone = phoneNumber,
            appHash = Constants.APP_HASH,
            channel = channel,
            requestSignature = createSignatureFieldB58(
                userPublicKey = userPublicKey,
                solanaPrivateKey = userPrivateKey,
                RegisterWalletSignatureStruct(
                    clientId = userPublicKey,
                    etheriumId = userPrivateKey,
                    phone = phoneNumber,
                    channelType = channel.backendName
                )
            ),
            timestamp = createTimestampField(),
        )
            .let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethods.REGISTER_WALLET) }
    }

    fun toConfirmRegisterWalletNetwork(
        userPublicKey: String,
        userPrivateKey: String,
        etheriumPublicKey: String,
        phoneNumber: String,
        otpConfirmationCode: String
    ): GatewayServiceRequest<ConfirmRegisterWalletRequest> {
        return ConfirmRegisterWalletRequest(
            clientSolanaPublicKeyB58 = userPublicKey,
            etheriumPublicKeyB58 = etheriumPublicKey,
            requestSignature = createSignatureFieldB58(
                userPublicKey = userPublicKey,
                solanaPrivateKey = userPrivateKey,
                ConfirmRegisterWalletSignatureStruct(
                    clientId = userPublicKey,
                    etheriumId = userPrivateKey,
                    phone = phoneNumber,
                    encryptedShare = TODO(),
                    phoneConfirmationCode = otpConfirmationCode
                )
            ),
            timestamp = createTimestampField(),
            encryptedOtpShare = TODO(),
            encryptedPayloadB64 = TODO(),
            otpConfirmationCode = otpConfirmationCode
        )
            .let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethods.CONFIRM_REGISTER_WALLET) }
    }

    private fun createSignatureFieldB58(
        userPublicKey: String,
        solanaPrivateKey: String,
        structToSerialize: Borsh
    ): String {
        try {
            val signatureStructBytes = Borsh.serialize(structToSerialize)
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
        return SimpleDateFormat(TIMESTAMP_PATTERN_GATEWAY_SERVICE, Locale.getDefault()).format(Date())
    }
}
