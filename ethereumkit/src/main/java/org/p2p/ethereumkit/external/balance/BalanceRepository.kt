package org.p2p.ethereumkit.external.balance

import java.math.BigInteger
import org.p2p.ethereumkit.external.api.response.TokenBalancesResponse
import org.p2p.ethereumkit.external.api.response.TokenMetadataResponse
import org.p2p.ethereumkit.internal.models.EthAddress

interface BalanceRepository {
    suspend fun getWalletBalance(address: EthAddress): BigInteger
    suspend fun getTokenBalances(address: EthAddress): TokenBalancesResponse
    suspend fun getTokenMetadata(contractAddresses: EthAddress): TokenMetadataResponse
}
