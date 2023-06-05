package org.p2p.wallet.send.model

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

sealed interface SendTokenModel {

    val mintAddress: Base58String
    val decimals: Int
    val tokenName: String
    val tokenSymbol: String
    val iconUrl: String?
    val rate: BigDecimal?
    val tokenAmount: BigDecimal
    val tokenAmountInUsd: BigDecimal?
    val totalInLamports: BigInteger

    fun isSol(): Boolean = mintAddress.base58Value == Constants.WRAPPED_SOL_MINT

    fun equalsByMint(other: SwapTokenModel?): Boolean = this.mintAddress == other?.mintAddress

    data class UserToken(
        val details: Token.Active,
    ) : SendTokenModel {
        override val decimals: Int = details.decimals
        override val mintAddress: Base58String = details.mintAddress.toBase58Instance()
        override val tokenName: String = details.tokenName
        override val tokenSymbol: String = details.tokenSymbol
        override val iconUrl: String? = details.iconUrl
        override val rate: BigDecimal? = details.rate
        override val tokenAmount: BigDecimal = details.total
        override val tokenAmountInUsd: BigDecimal? = details.totalInUsd
        override val totalInLamports: BigInteger = details.totalInLamports
    }

    data class EthereumToken(
        val details: Token.Eth,
    ) : SendTokenModel {
        override val decimals: Int = details.decimals
        override val mintAddress: Base58String = details.mintAddress.toBase58Instance()
        override val tokenName: String = details.tokenName
        override val iconUrl: String? = details.iconUrl
        override val tokenSymbol: String = details.tokenSymbol
        override val rate: BigDecimal? = details.rate
        override val tokenAmount: BigDecimal = details.total
        override val tokenAmountInUsd: BigDecimal? = details.totalInUsd
        override val totalInLamports: BigInteger = details.totalInLamports
    }
}
