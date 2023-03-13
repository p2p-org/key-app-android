package org.p2p.wallet.jupiter.ui.main

import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel

class SwapTokenRateNotFound(
    token: SwapTokenModel
) : Throwable() {
    override val message: String = buildString {
        append("Token rate not found for token ")
        append(token.tokenName)
        append("; ")
        append(token.mintAddress)
        append("; ")
        append(
            when (token) {
                is SwapTokenModel.JupiterToken -> token.coingeckoId
                is SwapTokenModel.UserToken -> null
            }
        )
        append("; ")
        append(token::class.simpleName)
        append("; ")
    }
}
