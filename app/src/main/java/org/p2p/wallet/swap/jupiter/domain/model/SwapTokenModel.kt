package org.p2p.wallet.swap.jupiter.domain.model

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

sealed interface SwapTokenModel {

    val mintAddress: Base58String
    val decimals: Int
    val tokenName: String
    val tokenSymbol: String
    val iconUrl: String?

    data class UserToken(
        val details: Token.Active,
    ) : SwapTokenModel {
        override val decimals: Int = details.decimals
        override val mintAddress: Base58String = details.mintAddress.toBase58Instance()
        override val tokenName: String = details.tokenName
        override val tokenSymbol: String = details.tokenSymbol
        override val iconUrl: String? = details.iconUrl
        val tokenAmount: BigDecimal = details.total
        val tokenAmountInUsd: BigDecimal? = details.totalInUsd
    }

    data class JupiterToken(
        val details: JupiterSwapToken,
    ) : SwapTokenModel {
        val coingeckoId: String? = details.coingeckoId
        override val decimals: Int = details.decimals
        override val mintAddress: Base58String = details.tokenMint
        override val tokenName: String = details.tokenName
        override val iconUrl: String? = details.logoUri
        override val tokenSymbol: String = details.tokenSymbol
    }
}
