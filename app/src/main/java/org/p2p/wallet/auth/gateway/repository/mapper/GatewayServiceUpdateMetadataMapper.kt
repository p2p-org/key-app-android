package org.p2p.wallet.auth.gateway.repository.mapper

import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.utils.toJsonObject
import org.p2p.core.crypto.toBase64Instance
import org.p2p.wallet.auth.gateway.api.request.GatewayOnboardingMetadataCiphered
import org.p2p.wallet.auth.gateway.api.request.UpdateMetadataRpcRequest
import org.p2p.wallet.auth.gateway.api.response.UpdateMetadataResponse
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.utils.toByteArray

class GatewayServiceUpdateMetadataMapper(
    private val onboardingMetadataCipher: GatewayServiceOnboardingMetadataCipher,
    private val signatureFieldGenerator: PushServiceSignatureFieldGenerator,
    private val gson: Gson
) {

    private class UpdateMetadataStruct(
        val ethereumAddress: String,
        val solanaPublicKey: String,
        val epochUnixTime: Long,
        val encryptedMetadata: String,
    ) : BorshSerializable {
        override fun serializeSelf(): ByteArray {
            return getBorshBuffer()
                .write(ethereumAddress, solanaPublicKey, epochUnixTime, encryptedMetadata)
                .toByteArray()
        }
    }

    fun toNetwork(
        userPrivateKey: Base58String,
        userPublicKey: Base58String,
        ethereumAddress: String,
        userSeedPhrase: List<String>,
        metadata: GatewayOnboardingMetadata,
    ): JsonRpc<Map<String, Any>, UpdateMetadataResponse> {
        val epochUnixTime = System.currentTimeMillis().milliseconds
        val encryptedMetadata: GatewayOnboardingMetadataCiphered = onboardingMetadataCipher.encryptMetadata(
            mnemonicPhrase = userSeedPhrase,
            onboardingMetadata = metadata
        )
        val encryptedMetadataJson = gson.toJsonObject(encryptedMetadata)
        val signatureField = signatureFieldGenerator.generateSignatureField(
            userPrivateKey = userPrivateKey,
            structToSerialize = UpdateMetadataStruct(
                ethereumAddress = ethereumAddress,
                solanaPublicKey = userPublicKey.base58Value,
                epochUnixTime = epochUnixTime.inWholeSeconds,
                encryptedMetadata = encryptedMetadataJson.toString()
            )
        )

        return UpdateMetadataRpcRequest(
            ethereumAddress = ethereumAddress,
            userPublicKey = userPublicKey,
            encryptedMetadata = encryptedMetadataJson.toByteArray().toBase64Instance(),
            requestSignature = signatureField.base58Value,
            timestamp = createTimestampField(epochUnixTime.inWholeMilliseconds)
        )
    }

    private fun createTimestampField(epochUnixTime: Long): String {
        return SimpleDateFormat(TIMESTAMP_PATTERN_GATEWAY_SERVICE, Locale.getDefault()).format(Date(epochUnixTime))
    }
}
