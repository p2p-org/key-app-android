package org.p2p.wallet.auth.gateway.repository.mapper

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.near.borshj.Borsh
import org.p2p.solanaj.utils.crypto.toBase64Instance
import org.p2p.wallet.auth.gateway.api.request.ConfirmRegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.request.GatewayOnboardingMetadataCiphered
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceJsonRpcMethod
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceRequest
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.request.RegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceResponse
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.gateway.repository.model.PushServiceError
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.utils.Base58String
import org.p2p.core.utils.Constants
import org.p2p.wallet.utils.toByteArray
import org.p2p.wallet.utils.toJsonObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import org.p2p.wallet.settings.DeviceInfoHelper

const val TIMESTAMP_PATTERN_GATEWAY_SERVICE = "yyyy-MM-dd HH:mm:ssXXX"

class GatewayServiceCreateWalletMapper(
    private val signatureFieldGenerator: PushServiceSignatureFieldGenerator,
    private val onboardingMetadataCipher: GatewayServiceOnboardingMetadataCipher,
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

    data class ConfirmRegisterWalletSignatureStruct(
        val clientId: String,
        val etheriumId: String,
        val encryptedShare: JsonObject,
        val encryptedPayload: JsonObject,
        val onboardingMetadata: JsonObject,
        val phone: String,
        val phoneConfirmationCode: String
    ) : BorshSerializable, Borsh {

        override fun serializeSelf(): ByteArray =
            getBorshBuffer()
                .write(
                    etheriumId,
                    clientId,
                    encryptedShare.toString(),
                    encryptedPayload.toString(),
                    onboardingMetadata.toString(),
                    phone,
                    phoneConfirmationCode
                ) // order is important
                .toByteArray()
    }

    @Throws(PushServiceError::class)
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
        phoneNumber: PhoneNumber,
        jsonEncryptedMnemonicPhrase: JsonObject,
        socialShareOwnerId: String,
        userSeedPhrase: List<String>,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        otpConfirmationCode: String
    ): GatewayServiceRequest<ConfirmRegisterWalletRequest> {
        val epochUnixTimeSeconds = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS).inWholeSeconds
        val encryptedMetadata: GatewayOnboardingMetadataCiphered = onboardingMetadataCipher.encryptMetadata(
            mnemonicPhrase = userSeedPhrase,
            onboardingMetadata = GatewayOnboardingMetadata(
                ethPublic = etheriumAddress.lowercase(),
                metaTimestampSec = epochUnixTimeSeconds,
                deviceShareDeviceName = DeviceInfoHelper.getCurrentDeviceName(),
                deviceNameTimestampSec = epochUnixTimeSeconds,
                customSharePhoneNumberE164 = phoneNumber.e164Formatted(),
                phoneNumberTimestampSec = epochUnixTimeSeconds,
                socialShareOwnerEmail = socialShareOwnerId,
                emailTimestampSec = epochUnixTimeSeconds,
            )
        )

        val signatureField: Base58String = signatureFieldGenerator.generateSignatureField(
            userPrivateKey = userPrivateKey,
            structToSerialize = ConfirmRegisterWalletSignatureStruct(
                clientId = userPublicKey.base58Value,
                etheriumId = etheriumAddress.lowercase(),
                phone = phoneNumber.e164Formatted(),
                encryptedShare = gson.toJsonObject(thirdShare),
                encryptedPayload = jsonEncryptedMnemonicPhrase,
                onboardingMetadata = gson.toJsonObject(encryptedMetadata),
                phoneConfirmationCode = otpConfirmationCode
            )
        )

        return ConfirmRegisterWalletRequest(
            clientSolanaPublicKey = userPublicKey,
            etheriumAddress = etheriumAddress,
            timestamp = createTimestampField(),
            thirdShare = gson.toJsonObject(thirdShare).toByteArray().toBase64Instance(),
            encryptedPayloadB64 = jsonEncryptedMnemonicPhrase.toByteArray().toBase64Instance(),
            onboardingMetadata = gson.toJsonObject(encryptedMetadata).toByteArray().toBase64Instance(),
            otpConfirmationCode = otpConfirmationCode,
            phone = phoneNumber.e164Formatted(),
            requestSignature = signatureField
        ).let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethod.CONFIRM_REGISTER_WALLET) }
    }

    private fun createTimestampField(): String {
        return SimpleDateFormat(TIMESTAMP_PATTERN_GATEWAY_SERVICE, Locale.getDefault()).format(Date())
    }
}
