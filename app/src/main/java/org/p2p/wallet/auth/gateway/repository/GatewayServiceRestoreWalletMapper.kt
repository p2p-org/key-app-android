package org.p2p.wallet.auth.gateway.repository

import org.near.borshj.Borsh
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.request.RestoreWalletRequest
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GatewayServiceRestoreWalletMapper(
    private val signatureFieldGenerator: GatewayServiceSignatureFieldGenerator
) {
    private class RestoreWalletSignatureStruct(
        val restoreId: String,
        val appHash: String,
        val phone: String,
        val channelType: String
    ) : Borsh

    fun toRestoreWalletNetwork(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        phoneNumber: String,
        channel: OtpMethod
    ): RestoreWalletRequest {
        val signatureField = signatureFieldGenerator.generateSignatureField(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            structToSerialize = RestoreWalletSignatureStruct(
                restoreId = userPublicKey.base58Value,
                appHash = Constants.APP_HASH,
                phone = phoneNumber,
                channelType = channel.backendName
            )
        )

        return RestoreWalletRequest(
            clientSolanaPublicKey = userPublicKey.base58Value,
            userPhone = phoneNumber,
            appHash = Constants.APP_HASH,
            channelMethod = channel,
            requestSignature = signatureField.base58Value,
            timestamp = createTimestampField()
        )
    }

    private fun createTimestampField(): String {
        return SimpleDateFormat(TIMESTAMP_PATTERN_GATEWAY_SERVICE, Locale.getDefault()).format(Date())
    }
}
