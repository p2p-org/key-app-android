package org.p2p.wallet.claim.repository

import org.p2p.wallet.claim.api.response.FeesResponse

interface EthereumClaimRepository {
    suspend fun getEthereumFees(
        ethereumAddress: String,
        solanaAddress: String,
        tokenAddress: String?,
        amountAsString: String
    ): FeesResponse
}
