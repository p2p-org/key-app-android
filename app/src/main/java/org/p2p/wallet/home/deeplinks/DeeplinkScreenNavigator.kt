package org.p2p.wallet.home.deeplinks

import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.SwapDeeplinkStrictTokenWarning

interface DeeplinkScreenNavigator {
    fun navigateToBuyScreen(token: Token, fiatToken: String, fiatAmount: String?)
    fun navigateToBuyScreen(token: Token)
    fun showCashOut()
    fun showSwapWithArgs(
        tokenAMint: Base58String,
        tokenBMint: Base58String,
        amountA: String,
        strictWarning: SwapDeeplinkStrictTokenWarning?,
        source: SwapOpenedFrom
    )
    fun showSwap()
}
