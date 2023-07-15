package org.p2p.wallet.home.deeplinks

import org.p2p.core.token.Token
import org.p2p.wallet.jupiter.model.SwapOpenedFrom

interface DeeplinkScreenNavigator {
    fun navigateToNewBuyScreen(token: Token, fiatToken: String, fiatAmount: String?)
    fun navigateToBuyScreen(token: Token)
    fun showTokensForBuy(tokens: List<Token>)
    fun showCashOut()
    fun showSwapWithArgs(tokenASymbol: String, tokenBSymbol: String, amountA: String, source: SwapOpenedFrom)
    fun showSwap()
}
