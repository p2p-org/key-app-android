package org.p2p.wallet.home.ui.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.p2p.wallet.deeplinks.DeeplinkData
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.home.ui.main.models.HomeScreenViewState
import org.p2p.wallet.infrastructure.coroutines.waitForCondition
import org.p2p.wallet.newsend.model.SearchOpenedFromScreen
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.user.interactor.UserInteractor

/**
 * Handles deeplinks supported by the home screen
 */
class HomePresenterDeeplinkHandler(
    private val coroutineScope: CoroutineScope,
    private val presenter: HomeContract.Presenter,
    private val view: HomeContract.View?,
    private val state: HomeScreenViewState,
    private val userInteractor: UserInteractor
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
        view?.showCashOut()
    }

    private fun handleSwapDeeplink(data: DeeplinkData) {
        if (data.args.containsKey("from") && data.args.containsKey("to")) {
            val amount = data.args["amount"]?.toBigDecimalOrNull()?.toPlainString() ?: "0"
            view?.showSwapWithArgs(
                tokenASymbol = data.args.getValue("from"),
                tokenBSymbol = data.args.getValue("to"),
                amountA = amount,
                source = SwapOpenedFrom.MAIN_SCREEN
            )
        } else {
            view?.showSwap(SwapOpenedFrom.MAIN_SCREEN)
        }
    }

    private suspend fun handleSendDeeplink() {
        // fixme hack! waiting for tokens to load, probably it's better to show progress
        waitForCondition(1000) { state.tokens.isNotEmpty() }
        presenter.onSendClicked(SearchOpenedFromScreen.MAIN)
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
                    view?.showNewBuyScreen(token, fiatToken, fiatAmount)
                } else {
                    presenter.onBuyClicked()
                }
            }
        } else {
            presenter.onBuyClicked()
        }
    }
}
