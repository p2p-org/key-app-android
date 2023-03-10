package org.p2p.ethereumkit.external.model

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.ethereumkit.external.api.alchemy.response.TokenBalanceResponse
import org.p2p.ethereumkit.external.api.alchemy.response.TokenMetadataResponse
import org.p2p.ethereumkit.internal.models.EthAddress

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
    erc20Token: ERC20Tokens
): EthTokenMetadata {

    return EthTokenMetadata(
        contractAddress = balanceResponse.contractAddress,
        mintAddress = erc20Token.mintAddress,
        balance = balanceResponse.tokenBalance,
        decimals = metadata.decimals,
        logoUrl = erc20Token.tokenIconUrl ?: metadata.logoUrl.orEmpty(),
        tokenName = erc20Token.replaceTokenName ?: metadata.tokenName.orEmpty(),
        symbol = erc20Token.replaceTokenSymbol ?: metadata.symbol.orEmpty()
    )
}
