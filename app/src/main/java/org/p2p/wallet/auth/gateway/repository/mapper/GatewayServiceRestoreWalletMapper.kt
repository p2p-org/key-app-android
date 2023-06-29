package org.p2p.wallet.auth.gateway.repository.mapper

import org.p2p.wallet.auth.gateway.api.request.ConfirmRestoreWalletRequest
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceJsonRpcMethod
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceRequest
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.request.RestoreWalletRequest
import org.p2p.core.crypto.Base58String
import org.p2p.core.utils.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GatewayServiceRestoreWalletMapper(
    private val signatureFieldGenerator: PushServiceSignatureFieldGenerator
) {
    private class RestoreWalletSignatureStruct(
        val restoreId: String,
        val appHash: String,
        val phone: String,
        val channelType: String
    ) : BorshSerializable {
        override fun serializeSelf(): ByteArray =
            getBorshBuffer()
                .write(restoreId, phone, appHash, channelType)
                .toByteArray()
    }

    private class ConfirmRestoreWalletSignatureStruct(
        val restoreId: String,
        val phone: String,
        val otpConfirmationCode: String
    ) : BorshSerializable {
        override fun serializeSelf(): ByteArray =
            getBorshBuffer()
                .write(restoreId, phone, otpConfirmationCode)
                .toByteArray()
    }

    fun toRestoreWalletNetwork(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        phoneNumber: String,
        channel: OtpMethod
    ): GatewayServiceRequest<RestoreWalletRequest> {
        val signatureField = signatureFieldGenerator.generateSignatureField(
            userPrivateKey = userPrivateKey,
            structToSerialize = RestoreWalletSignatureStruct(
                restoreId = userPublicKey.base58Value,
                appHash = Constants.APP_HASH,
                phone = phoneNumber,
                channelType = channel.backendName
            )
        )

        return RestoreWalletRequest(
            temporarySolanaPublicKey = userPublicKey.base58Value,
            userPhone = phoneNumber,
            appHash = Constants.APP_HASH,
            channelMethod = channel,
            requestSignature = signatureField.base58Value,
            timestamp = createTimestampField()
        )
            .let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethod.RESTORE_WALLET) }
    }

    fun toConfirmRestoreWalletNetwork(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        phoneNumber: String,
        otpConfirmationCode: String
    ): GatewayServiceRequest<ConfirmRestoreWalletRequest> {
        val signatureField = signatureFieldGenerator.generateSignatureField(
            userPrivateKey = userPrivateKey,
            structToSerialize = ConfirmRestoreWalletSignatureStruct(
                restoreId = userPublicKey.base58Value,
                phone = phoneNumber,
                otpConfirmationCode = otpConfirmationCode
            )
        )

        return ConfirmRestoreWalletRequest(
            temporarySolanaPublicKey = userPublicKey.base58Value,
            userPhone = phoneNumber,
            requestSignature = signatureField.base58Value,
            otpConfirmationCode = otpConfirmationCode,
            timestamp = createTimestampField()
        )
            .let { GatewayServiceRequest(it, methodName = GatewayServiceJsonRpcMethod.CONFIRM_RESTORE_WALLET) }
    }

    private fun createTimestampField(): String {
        return SimpleDateFormat(TIMESTAMP_PATTERN_GATEWAY_SERVICE, Locale.getDefault()).format(Date())
    }
}
