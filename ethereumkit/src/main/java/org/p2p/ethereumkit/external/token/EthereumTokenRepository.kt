package org.p2p.ethereumkit.external.token

import java.math.BigInteger
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.api.alchemy.response.TokenBalancesResponse
import org.p2p.ethereumkit.external.api.alchemy.response.TokenMetadataResponse

internal interface EthereumTokenRepository {
    suspend fun getWalletBalance(address: EthAddress): BigInteger
    suspend fun getTokenBalances(
        address: EthAddress,
        tokenAddresses: List<EthAddress>
    ): TokenBalancesResponse

    suspend fun getTokenMetadata(contractAddresses: EthAddress): TokenMetadataResponse
}
