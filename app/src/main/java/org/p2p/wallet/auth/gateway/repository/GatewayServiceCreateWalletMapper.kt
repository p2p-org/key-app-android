package org.p2p.wallet.auth.gateway.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.p2p.solanaj.utils.crypto.encodeToBase64
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
    private val errorMapper: GatewayServiceErrorMapper,
    private val gson: Gson
) {
    private data class RegisterWalletSignatureStruct(
        val etheriumId: String,
        val clientId: String,
        val phone: String,
        val appHash: String,
        val channelType: String,
    ) : BorshSerializable {
        override fun serializeSelf(): ByteArray =
            getBorshBuffer()
                .write(etheriumId, clientId, phone, appHash, channelType) // order is important
                .toByteArray()
    }

    private data class ConfirmRegisterWalletSignatureStruct(
        val clientId: String,
        val etheriumId: String,
        val encryptedShare: String,
        val encryptedPayload: String,
        val encryptedMetadata: String = "-",
        val phone: String,
        val phoneConfirmationCode: String
    ) : BorshSerializable {
        override fun serializeSelf(): ByteArray =
            getBorshBuffer()
                .write(
                    etheriumId,
                    clientId,
                    encryptedShare,
                    encryptedPayload,
                    encryptedMetadata,
                    phone,
                    phoneConfirmationCode
                ) // order is important
                .toByteArray()
    }

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
        etheriumAddress: String,
        phoneNumber: String,
        channel: OtpMethod
    ): GatewayServiceRequest<RegisterWalletRequest> {
        val signatureField: Base58String = signatureFieldGenerator.generateSignatureField(
            userPrivateKey = userPrivateKey,
            structToSerialize = RegisterWalletSignatureStruct(
                clientId = userPublicKey.base58Value,
                etheriumId = etheriumAddress.lowercase(),
                phone = phoneNumber,
                channelType = channel.backendName,
                appHash = Constants.APP_HASH
            )
        )

        return RegisterWalletRequest(
            clientSolanaPublicKey = userPublicKey,
            etheriumAddress = etheriumAddress,
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
        etheriumAddress: String,
        phoneNumber: String,
        jsonEncryptedMnemonicPhrase: JsonObject,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        otpConfirmationCode: String
    ): GatewayServiceRequest<ConfirmRegisterWalletRequest> {

        val encryptedPayloadStrJson = jsonEncryptedMnemonicPhrase.toString()

        val signatureField: Base58String = signatureFieldGenerator.generateSignatureField(
            userPrivateKey = userPrivateKey,
            structToSerialize = ConfirmRegisterWalletSignatureStruct(
                clientId = userPublicKey.base58Value,
                etheriumId = etheriumAddress.lowercase(),
                phone = phoneNumber,
                encryptedShare = gson.toJson(thirdShare),
                encryptedPayload = encryptedPayloadStrJson,
                phoneConfirmationCode = otpConfirmationCode
            )
        )

        return ConfirmRegisterWalletRequest(
            clientSolanaPublicKey = userPublicKey.base58Value,
            etheriumAddress = etheriumAddress,
            timestamp = createTimestampField(),
            thirdShare = gson.toJson(thirdShare).toByteArray().encodeToBase64(),
            encryptedMetadata = "-".toByteArray().encodeToBase64(), // todo: replace in PWN-5213
            encryptedPayloadB64 = encryptedPayloadStrJson.toByteArray().encodeToBase64(),
            otpConfirmationCode = otpConfirmationCode,
            phone = phoneNumber,
            requestSignature = signatureField.base58Value
        ).let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethod.CONFIRM_REGISTER_WALLET) }
    }

    private fun createTimestampField(): String {
        return SimpleDateFormat(TIMESTAMP_PATTERN_GATEWAY_SERVICE, Locale.getDefault()).format(Date())
    }
}
