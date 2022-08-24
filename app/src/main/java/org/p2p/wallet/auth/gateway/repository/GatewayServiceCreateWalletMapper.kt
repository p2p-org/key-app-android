package org.p2p.wallet.auth.gateway.repository

import com.google.gson.JsonObject
import org.near.borshj.Borsh
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.solanaj.utils.crypto.encodeToBase64String
import org.p2p.wallet.auth.gateway.api.request.ConfirmRegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceJsonRpcMethod
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceRequest
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.request.RegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val TIMESTAMP_PATTERN_GATEWAY_SERVICE = "yyyy-MM-dd HH:mm:ssXXX"

class GatewayServiceCreateWalletMapper(
    private val signatureFieldGenerator: GatewayServiceSignatureFieldGenerator,
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
        val signatureField: Base58String = signatureFieldGenerator.generateSignatureField(
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
            .let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethod.REGISTER_WALLET) }
    }

    fun toConfirmRegisterWalletNetwork(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        phoneNumber: String,
        jsonEncryptedMnemonicPhrase: JsonObject,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta.ShareInnerDetails.ShareValue,
        otpConfirmationCode: String
    ): GatewayServiceRequest<ConfirmRegisterWalletRequest> {
        val signatureField: Base58String = signatureFieldGenerator.generateSignatureField(
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
            .let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethod.CONFIRM_REGISTER_WALLET) }
    }

    private fun createTimestampField(): String {
        return SimpleDateFormat(TIMESTAMP_PATTERN_GATEWAY_SERVICE, Locale.getDefault()).format(Date())
    }
}
