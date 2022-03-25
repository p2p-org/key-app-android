package org.p2p.wallet.rpc.repository.signature

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.SignatureInformationResponse

interface RpcSignatureRepository {
    suspend fun getConfirmedSignaturesForAddress(
        userAccountAddress: PublicKey,
        before: String?,
        limit: Int
    ): List<SignatureInformationResponse>
}
