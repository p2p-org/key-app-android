package org.p2p.wallet.rpc.repository.signature

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.SignatureInformation

interface RpcSignatureRepository {
    suspend fun getConfirmedSignaturesForAddress(
        account: PublicKey,
        before: String?,
        limit: Int
    ): List<SignatureInformation>
}
