package org.p2p.wallet.auth.gateway.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.near.borshj.Borsh
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.solanaj.utils.crypto.encodeToBase64String
import org.p2p.wallet.auth.gateway.api.request.ConfirmRegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceJsonRpcMethods
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceRequest
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.request.RegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceResponse
import org.p2p.wallet.auth.model.Web3AuthSignUpResponse
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: PWN-4362 add correct date format
private const val TIMESTAMP_PATTERN_GATEWAY_SERVICE = "yyyy-MM-dd HH:mm:ss"

class GatewayServiceMapper(
    private val gson: Gson,
    private val errorMapper: GatewayServiceErrorMapper
) {
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
            throw errorMapper.fromNetwork(response.errorBody)
        }
        return response.result!!
    }

    fun toRegisterWalletNetwork(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        phoneNumber: String,
        channel: OtpMethod
    ): GatewayServiceRequest<RegisterWalletRequest> {
        val signatureField: Base58String = createSignatureField(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            structToSerialize = RegisterWalletSignatureStruct(
                clientId = userPublicKey.base58Value,
                etheriumId = userPrivateKey.base58Value,
                phone = phoneNumber,
                channelType = channel.backendName
            )
        )

        return RegisterWalletRequest(
            clientSolanaPublicKey = userPublicKey,
            etheriumPublicKey = etheriumPublicKey,
            userPhone = phoneNumber,
            appHash = Constants.APP_HASH,
            channel = channel,
            timestamp = createTimestampField(),
            requestSignature = signatureField.base58Value
        )
            .let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethods.REGISTER_WALLET) }
    }

    fun toConfirmRegisterWalletNetwork(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        phoneNumber: String,
        jsonEncryptedMnemonicPhrase: JsonObject,
        thirdShare: Web3AuthSignUpResponse.ShareRootDetails.ShareInnerDetails.ShareValue,
        otpConfirmationCode: String
    ): GatewayServiceRequest<ConfirmRegisterWalletRequest> {
        val signatureField: Base58String = createSignatureField(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            structToSerialize = ConfirmRegisterWalletSignatureStruct(
                clientId = userPublicKey.base58Value,
                etheriumId = etheriumPublicKey,
                phone = phoneNumber,
                encryptedShare = thirdShare.value,
                phoneConfirmationCode = otpConfirmationCode
            )
        )

        val encryptedPayload: Base64String = jsonEncryptedMnemonicPhrase.toString()
            .toByteArray()
            .encodeToBase64String()

        return ConfirmRegisterWalletRequest(
            clientSolanaPublicKey = userPublicKey.base58Value,
            etheriumPublicKey = etheriumPublicKey,
            timestamp = createTimestampField(),
            thirdShare = thirdShare.value,
            encryptedPayloadB64 = encryptedPayload.base64Value,
            otpConfirmationCode = otpConfirmationCode,
            requestSignature = signatureField.base58Value
        )
            .let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethods.CONFIRM_REGISTER_WALLET) }
    }

    private fun createSignatureField(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        structToSerialize: Borsh
    ): Base58String {
        try {
            val signatureStructBytes = Borsh.serialize(structToSerialize)
            val userPublicKeyBytes = userPublicKey.decodeToBytes()
            val solanaPrivateKeyBytes = userPrivateKey.decodeToBytes()
            return TweetNaclFast.Signature(userPublicKeyBytes.copyOf(), solanaPrivateKeyBytes.copyOf())
                .sign(signatureStructBytes)
                .toBase58Instance()
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
