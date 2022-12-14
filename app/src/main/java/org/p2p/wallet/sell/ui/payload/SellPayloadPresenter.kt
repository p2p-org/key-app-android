package org.p2p.wallet.sell.ui.payload

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.orZero
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellFiatCurrency
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber

private const val SELL_QUOTE_REQUEST_DEBOUNCE_TIME = 10_000L

class SellPayloadPresenter(
    private val sellInteractor: SellInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<SellPayloadContract.View>(),
    SellPayloadContract.Presenter {

    private var userSolToken: Token.Active? = null
    private var minSellAmount: Double = 0.0
    private var fiat: MoonpaySellFiatCurrency? = null

    override fun load() {
        launch {
            try {
                view?.showLoading(isVisible = true)
                if (isUserHasTransactionsInProcess()) {
                    view?.navigateToSellLock()
                    return@launch
                }
                userSolToken = userInteractor.getUserSolToken()
                loadCurrencies()
                checkForMinAmount()
                startLoadSellQuoteJob()
            } catch (e: Throwable) {
                Timber.e("Error on init view $e")
                view?.showErrorScreen()
            } finally {
                view?.showLoading(isVisible = false)
            }
        }
    }

    private suspend fun isUserHasTransactionsInProcess(): Boolean {
        val userTransactions = sellInteractor.loadUserSellTransactions()
        return userTransactions.isNotEmpty() && userTransactions.all {
            it.status == MoonpaySellTransaction.TransactionStatus.COMPLETED
        }
    }

    private suspend fun loadCurrencies() {
        val solCurrency = sellInteractor.getAllCurrencies().firstOrNull { it.isSol() }
        minSellAmount = solCurrency?.amounts?.minSellAmount ?: 0.0
        fiat = sellInteractor.getMoonpaySellFiatCurrency()
    }

    private fun checkForMinAmount() {
        if (userSolToken?.total.orZero() < minSellAmount.toBigDecimal()) {
            // view?.showNotEnoughMoney(minSellAmount)
        }
    }

    private fun startLoadSellQuoteJob() {
        launch {
            while (isActive) {
                try {
                    val selectedFiat = fiat ?: error("Fiat cannot be null")
                    val sellQuote = sellInteractor.getSellQuoteForSol(
                        solAmount = minSellAmount,
                        fiat = selectedFiat
                    )
                    val quoteCurrencyAmount = sellQuote.fiatEarning / sellQuote.tokenAmount
                    val fee = sellQuote.feeAmount
                    view?.updateValues(quoteCurrencyAmount, fee)
                    delay(SELL_QUOTE_REQUEST_DEBOUNCE_TIME)
                } catch (e: Throwable) {
                    Timber.e("Error on init view $e")
                    view?.showErrorScreen()
                }
            }
        }
    }

    override fun cashOut() {}
}
