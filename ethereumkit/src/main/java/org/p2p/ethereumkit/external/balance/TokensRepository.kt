package org.p2p.ethereumkit.external.balance

import java.math.BigInteger
import org.p2p.ethereumkit.external.api.alchemy.response.TokenBalancesResponse
import org.p2p.ethereumkit.external.api.alchemy.response.TokenMetadataResponse
import org.p2p.ethereumkit.internal.models.EthAddress

internal interface TokensRepository {
    suspend fun getWalletBalance(address: EthAddress): BigInteger
    suspend fun getTokenBalances(
        address: EthAddress,
        tokenAddresses: List<EthAddress>
    ): TokenBalancesResponse

    suspend fun getTokenMetadata(contractAddresses: EthAddress): TokenMetadataResponse
}
