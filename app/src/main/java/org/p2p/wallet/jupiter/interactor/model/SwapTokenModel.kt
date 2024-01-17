package org.p2p.wallet.jupiter.interactor.model

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.core.utils.Constants.USDT_SYMBOL
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken

sealed interface SwapTokenModel {

    val mintAddress: Base58String
    val decimals: Int
    val tokenName: String
    val tokenSymbol: String
    val iconUrl: String?

    fun isStableCoin(): Boolean = tokenSymbol == USDC_SYMBOL || tokenSymbol == USDT_SYMBOL

    fun isWrappedSol(): Boolean = mintAddress.base58Value == WRAPPED_SOL_MINT

    fun equalsByMint(other: SwapTokenModel?): Boolean = this.mintAddress == other?.mintAddress

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
        val tokenAmountInLamports: BigInteger = details.totalInLamports
    }

    data class JupiterToken constructor(
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
