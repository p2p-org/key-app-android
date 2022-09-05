package org.p2p.wallet.auth.gateway.repository

import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

class GatewayServiceSignatureFieldGenerator {
    @Throws(GatewayServiceError.RequestCreationFailure::class)
    fun generateSignatureField(
        userPrivateKey: Base58String,
        structToSerialize: BorshSerializable
    ): Base58String = try {
        val signatureStructBytes = structToSerialize.serializeSelf()
        val solanaPrivateKeyBytes = userPrivateKey.decodeToBytes()
        TweetNaclFast.Signature(byteArrayOf(), solanaPrivateKeyBytes.copyOf())
            .detached(signatureStructBytes)
            .toBase58Instance()
    } catch (error: Throwable) {
        Timber.i(error)
        throw GatewayServiceError.RequestCreationFailure(
            message = "Failed to generate signature field",
            cause = error
        )
    }
}
