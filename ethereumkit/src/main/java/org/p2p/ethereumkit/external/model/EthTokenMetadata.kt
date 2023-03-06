package org.p2p.ethereumkit.external.model

import org.p2p.ethereumkit.external.api.alchemy.response.TokenBalanceResponse
import org.p2p.ethereumkit.external.api.alchemy.response.TokenMetadataResponse
import org.p2p.ethereumkit.internal.models.EthAddress
import java.math.BigDecimal
import java.math.BigInteger

data class EthTokenMetadata(
    val contractAddress: EthAddress,
    val mintAddress: String,
    val balance: BigInteger,
    val decimals: Int,
    val logoUrl: String,
    val tokenName: String,
    val symbol: String,
    var price: BigDecimal = BigDecimal.ZERO
)

internal fun mapToTokenMetadata(
    balanceResponse: TokenBalanceResponse,
    metadata: TokenMetadataResponse,
    mintAddress: String
): EthTokenMetadata {
    return  EthTokenMetadata(
        contractAddress = balanceResponse.contractAddress,
        mintAddress = mintAddress,
        balance = balanceResponse.tokenBalance,
        decimals = metadata.decimals,
        logoUrl = metadata.logoUrl.orEmpty(),
        tokenName = metadata.tokenName.orEmpty(),
        symbol = metadata.symbol.orEmpty()
    )
}
