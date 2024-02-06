package org.p2p.wallet.home.deeplinks

import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.p2p.core.crypto.toBase58Instance
import org.p2p.wallet.deeplinks.DeeplinkData
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.deeplinks.ReferralDeeplinkHandler
import org.p2p.wallet.deeplinks.SwapDeeplinkData
import org.p2p.wallet.deeplinks.SwapDeeplinkHandler
import org.p2p.wallet.infrastructure.coroutines.waitForCondition
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.SwapDeeplinkStrictTokenWarning
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.emptyString

/**
 * Handles deeplinks supported by main
 * @param deeplinkTopLevelHandler - if case is already done in implementation on a top level you can just pass it up
 */
class MainFragmentDeeplinkHandler(
    private val coroutineScope: CoroutineScope,
    private val screenNavigator: DeeplinkScreenNavigator?,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val userInteractor: UserInteractor,
    private val swapDeeplinkHandler: SwapDeeplinkHandler,
    private val deeplinkTopLevelHandler: (target: DeeplinkTarget) -> Unit,
    private val referralDeeplinkHandler: ReferralDeeplinkHandler,
) {

    suspend fun handle(data: DeeplinkData) {
        Timber.i("MainFragmentDeeplinkHandler received target: ${data.target}")
        when (data.target) {
            DeeplinkTarget.BUY -> handleBuyDeeplink(data)
            DeeplinkTarget.SEND -> handleSendDeeplink()
            DeeplinkTarget.SWAP -> handleSwapDeeplink(data)
            DeeplinkTarget.CASH_OUT -> handleCashOutDeeplink()
            DeeplinkTarget.REFERRAL -> handleReferralDeeplink(data)
            else -> Unit
        }
    }

    private fun handleCashOutDeeplink() {
        screenNavigator?.showCashOut()
    }

    private suspend fun handleSwapDeeplink(data: DeeplinkData) {
        when (val parsedData = swapDeeplinkHandler.parseDeeplink(data)) {
            is SwapDeeplinkData.TokensFound -> {
                screenNavigator?.showSwapWithArgs(
                    tokenAMint = parsedData.tokenAMint,
                    tokenBMint = parsedData.tokenBMint,
                    amountA = "0",
                    strictWarning = null,
                    source = SwapOpenedFrom.MAIN_SCREEN
                )
            }
            is SwapDeeplinkData.NonStrictTokensFound -> {
                // pass empty strings to rollback tokens to default values
                screenNavigator?.showSwapWithArgs(
                    tokenAMint = emptyString().toBase58Instance(),
                    tokenBMint = emptyString().toBase58Instance(),
                    amountA = "0",
                    strictWarning = SwapDeeplinkStrictTokenWarning(
                        notStrictTokenASymbol = parsedData.nonStrictTokenASymbol,
                        notStrictTokenBSymbol = parsedData.nonStrictTokenBSymbol
                    ),
                    source = SwapOpenedFrom.MAIN_SCREEN
                )
            }
            SwapDeeplinkData.TokensNotFound -> {
                screenNavigator?.showSwap()
            }
        }
    }

    private fun handleReferralDeeplink(data: DeeplinkData) {
        referralDeeplinkHandler.handleDeeplink(data)
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
                    screenNavigator?.navigateToBuyScreen(token, fiatToken, fiatAmount)
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
