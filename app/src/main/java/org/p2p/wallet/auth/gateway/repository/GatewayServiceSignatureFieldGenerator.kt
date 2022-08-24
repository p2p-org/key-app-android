package org.p2p.wallet.auth.gateway.repository

import org.near.borshj.Borsh
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

class GatewayServiceSignatureFieldGenerator {
    @Throws(GatewayServiceError.RequestCreationFailure::class)
    fun generateSignatureField(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        structToSerialize: Borsh
    ): Base58String = try {
        val signatureStructBytes = Borsh.serialize(structToSerialize)
        val userPublicKeyBytes = userPublicKey.decodeToBytes()
        val solanaPrivateKeyBytes = userPrivateKey.decodeToBytes()
        TweetNaclFast.Signature(userPublicKeyBytes.copyOf(), solanaPrivateKeyBytes.copyOf())
            .sign(signatureStructBytes)
            .toBase58Instance()
    } catch (error: Throwable) {
        Timber.i(error)
        throw GatewayServiceError.RequestCreationFailure(
            message = "Failed to generate signature field",
            cause = error
        )
    }
}
