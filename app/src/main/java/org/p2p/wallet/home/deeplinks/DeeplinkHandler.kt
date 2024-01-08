package org.p2p.wallet.home.deeplinks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.p2p.core.crypto.toBase58Instance
import org.p2p.wallet.deeplinks.DeeplinkData
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.infrastructure.coroutines.waitForCondition
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.user.interactor.UserInteractor

/**
 * Handles deeplinks supported by main
 * @param deeplinkTopLevelHandler - if case is already done in implementation on a top level you can just pass it up
 */
class DeeplinkHandler(
    private val coroutineScope: CoroutineScope,
    private val screenNavigator: DeeplinkScreenNavigator?,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val userInteractor: UserInteractor,
    private val deeplinkTopLevelHandler: (target: DeeplinkTarget) -> Unit,
) {

    suspend fun handle(data: DeeplinkData) {
        when (data.target) {
            DeeplinkTarget.BUY -> handleBuyDeeplink(data)
            DeeplinkTarget.SEND -> handleSendDeeplink()
            DeeplinkTarget.SWAP -> handleSwapDeeplink(data)
            DeeplinkTarget.CASH_OUT -> handleCashOutDeeplink()
            else -> Unit
        }
    }

    private fun handleCashOutDeeplink() {
        screenNavigator?.showCashOut()
    }

    private fun handleSwapDeeplink(data: DeeplinkData) {
        if (data.args.containsKey("inputMint") && data.args.containsKey("outputMint")) {
            val amount = data.args["amount"]?.toBigDecimalOrNull()?.toPlainString() ?: "0"
            screenNavigator?.showSwapWithArgs(
                tokenAMint = data.args.getValue("inputMint").toBase58Instance(),
                tokenBMint = data.args.getValue("outputMint").toBase58Instance(),
                amountA = amount,
                source = SwapOpenedFrom.MAIN_SCREEN
            )
        } else {
            screenNavigator?.showSwap()
        }
    }

    private suspend fun handleSendDeeplink() {
        waitForCondition(1000) { tokenServiceCoordinator.getUserTokens().isNotEmpty() }
        deeplinkTopLevelHandler(DeeplinkTarget.SEND)
    }

    /**
     * Handles buy token for fiat deeplink
     */
    private fun handleBuyDeeplink(data: DeeplinkData) {
        val cryptoToken = data.args["to"]
        val fiatToken = data.args["from"]
        val fiatAmount = data.args["amount"]

        if (!cryptoToken.isNullOrBlank() && !fiatToken.isNullOrBlank()) {
            coroutineScope.launch {
                val token = userInteractor.getSingleTokenForBuy(listOf(cryptoToken))
                if (token != null) {
                    screenNavigator?.navigateToNewBuyScreen(token, fiatToken, fiatAmount)
                } else {
                    onBuyHandled()
                }
            }
        } else {
            onBuyHandled()
        }
    }

    private fun onBuyHandled() {
        coroutineScope.launch {
            val tokensForBuy = userInteractor.getTokensForBuy()
            if (tokensForBuy.isEmpty()) return@launch

            screenNavigator?.navigateToBuyScreen(tokensForBuy.first())
        }
    }
}
