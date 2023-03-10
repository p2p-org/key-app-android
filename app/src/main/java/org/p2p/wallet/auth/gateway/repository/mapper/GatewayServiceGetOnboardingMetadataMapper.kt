package org.p2p.wallet.auth.gateway.repository.mapper

import com.google.gson.Gson
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.auth.gateway.api.request.GatewayOnboardingMetadataCiphered
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceJsonRpcMethod
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceRequest
import org.p2p.wallet.auth.gateway.api.request.GetOnboardingMetadataRequest
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.utils.Base58String
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GatewayServiceGetOnboardingMetadataMapper(
    private val onboardingMetadataCipher: GatewayServiceOnboardingMetadataCipher,
    private val signatureFieldGenerator: PushServiceSignatureFieldGenerator,
    private val gson: Gson
) {

    private class GetOnboardingMetadataStruct(
        val etheriumAddress: String,
        val solanaPublicKey: String,
        val epochUnixTime: Long
    ) : BorshSerializable {
        override fun serializeSelf(): ByteArray {
            return getBorshBuffer()
                .write(etheriumAddress, solanaPublicKey, epochUnixTime)
                .toByteArray()
        }
    }

    fun toNetwork(
        userPrivateKey: Base58String,
        userPublicKey: Base58String,
        etheriumAddress: String
    ): GatewayServiceRequest<GetOnboardingMetadataRequest> {
        val epochUnixTime = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
        val signatureField = signatureFieldGenerator.generateSignatureField(
            userPrivateKey = userPrivateKey,
            structToSerialize = GetOnboardingMetadataStruct(
                etheriumAddress = etheriumAddress,
                solanaPublicKey = userPublicKey.base58Value,
                epochUnixTime = epochUnixTime.inWholeSeconds
            )
        )

        return GetOnboardingMetadataRequest(
            etheriumAddress = etheriumAddress,
            userPublicKey = userPublicKey,
            requestSignature = signatureField.base58Value,
            timestamp = createTimestampField(epochUnixTime.inWholeMilliseconds)
        )
            .let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethod.GET_ONBOARDING_METADATA) }
    }

    fun fromNetwork(
        userSeedPhrase: List<String>,
        metadataCipheredFromService: Base64String
    ): GatewayOnboardingMetadata {
        val metadataAsJson = metadataCipheredFromService.decodeToBytes().toString(Charset.defaultCharset())
        val metadataCiphered = gson.fromJson(metadataAsJson, GatewayOnboardingMetadataCiphered::class.java)
        return onboardingMetadataCipher.decryptMetadata(userSeedPhrase, metadataCiphered)
    }

    private fun createTimestampField(epochUnixTime: Long): String {
        return SimpleDateFormat(TIMESTAMP_PATTERN_GATEWAY_SERVICE, Locale.getDefault()).format(Date(epochUnixTime))
    }
}
