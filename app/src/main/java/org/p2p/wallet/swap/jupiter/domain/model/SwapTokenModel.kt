package org.p2p.wallet.swap.jupiter.domain.model

import org.p2p.core.token.Token
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

sealed interface SwapTokenModel {

    val mintAddress: Base58String
    val decimals: Int
    val tokenName: String

    data class UserToken(
        val details: Token.Active,
    ) : SwapTokenModel {
        override val decimals: Int = details.decimals
        override val mintAddress: Base58String = details.mintAddress.toBase58Instance()
        override val tokenName: String = details.tokenName
    }

    data class JupiterToken(
        val details: JupiterSwapToken,
        val iconUrl: String?,
    ) : SwapTokenModel {
        override val decimals: Int = details.decimals
        override val mintAddress: Base58String = details.tokenMint
        override val tokenName: String = details.tokenName
    }
}
