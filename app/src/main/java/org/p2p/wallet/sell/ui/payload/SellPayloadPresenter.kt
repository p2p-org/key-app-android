package org.p2p.wallet.sell.ui.payload

import org.p2p.core.utils.Constants
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.formatTokenForMoonpay
import org.p2p.core.utils.formatUsd
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.moonpay.model.MoonpayWidgetUrlBuilder
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellFiatCurrency
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.sell.ui.lock.SellTransactionDetails
import org.p2p.wallet.sell.ui.payload.SellPayloadContract.ViewState
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val SELL_QUOTE_REQUEST_DEBOUNCE_TIME = 10_000L

class SellPayloadPresenter(
    private val sellInteractor: SellInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val moonpayWidgetUrlBuilder: MoonpayWidgetUrlBuilder,
    private val userInteractor: UserInteractor,
    private val resourceProvider: ResourcesProvider
) : BasePresenter<SellPayloadContract.View>(),
    SellPayloadContract.Presenter {

    private var userSolBalance: BigDecimal = BigDecimal.ZERO
    private var minTokenSellAmount: BigDecimal = BigDecimal.ZERO
    private var maxTokenSellAmount: BigDecimal? = null
    private var rawUserSelectedAmount: String = emptyString()
    private val userSelectedAmount: BigDecimal
        get() = rawUserSelectedAmount.toBigDecimalOrZero()
    private var currentFiat: MoonpaySellFiatCurrency = MoonpaySellFiatCurrency.USD
    private var tokenPrice: BigDecimal = BigDecimal.ZERO

    private var sellQuoteJob: Job? = null

    override fun attach(view: SellPayloadContract.View) {
        super.attach(view)
        launch {
            try {
                view.showLoading(isVisible = true)
                checkForSellLock()
                userSolBalance = userInteractor.getUserSolToken()?.total.orZero()
                loadCurrencies()
                checkForMinAmount()
                restartLoadSellQuoteJob()
            } catch (e: Throwable) {
                Timber.e(e, "Error on loading data from Moonpay")
                view.navigateToErrorScreen()
            } finally {
                view.showLoading(isVisible = false)
            }
        }
    }

    private suspend fun checkForSellLock() {
        val userTransactionInProcess = getUserTransactionInProcess()
        if (userTransactionInProcess != null) {
            // make readable in https://p2pvalidator.atlassian.net/browse/PWN-6354
            val amounts = userTransactionInProcess.amounts

            view?.navigateToSellLock(
                SellTransactionDetails(
                    userTransactionInProcess.status,
                    amounts.tokenAmount.formatTokenForMoonpay(),
                    amounts.usdAmount.formatUsd(),
                    Constants.REN_BTC_DEVNET_MINT // replace with actual address later
                )
            )
        }
    }

    private suspend fun getUserTransactionInProcess(): MoonpaySellTransaction? {
        val userTransactions = sellInteractor.loadUserSellTransactions()
        return userTransactions.find { it.status == MoonpaySellTransaction.SellTransactionStatus.WAITING_FOR_DEPOSIT }
    }

    private suspend fun loadCurrencies() {
        val solCurrency = sellInteractor.getSolCurrency()
        minTokenSellAmount = solCurrency.amounts.minSellAmount.orZero()
        maxTokenSellAmount = solCurrency.amounts.maxSellAmount
        rawUserSelectedAmount = minTokenSellAmount.formatToken()
        currentFiat = sellInteractor.getMoonpaySellFiatCurrency()
    }

    private fun checkForMinAmount() {
        if (userSolBalance < minTokenSellAmount) {
            view?.showNotEnoughMoney(minTokenSellAmount)
        }
    }

    private fun restartLoadSellQuoteJob() {
        sellQuoteJob?.cancel()

        sellQuoteJob = launch {
            while (isActive) {
                try {
                    val sellQuote = sellInteractor.getSellQuoteForSol(userSelectedAmount, currentFiat)

                    tokenPrice = sellQuote.tokenPrice

                    val fiatUiSymbol = currentFiat.uiSymbol
                    val moonpayFee = sellQuote.feeAmount.formatUsd()

                    val viewState = ViewState(
                        formattedUserAvailableBalance = userSolBalance.formatTokenForMoonpay(),
                        solToSell = rawUserSelectedAmount,
                        formattedFiatAmount = sellQuote.fiatEarning.formatUsd(),
                        formattedSellFiatFee = moonpayFee,
                        formattedTokenPrice = tokenPrice.formatUsd(),
                        fiatSymbol = fiatUiSymbol,
                        tokenSymbol = Constants.SOL_SYMBOL,
                    )

                    view?.updateViewState(viewState)
                    delay(SELL_QUOTE_REQUEST_DEBOUNCE_TIME)
                } catch (e: CancellationException) {
                    Timber.i(e)
                } catch (e: Throwable) {
                    Timber.e(e, "Error on loading data from Moonpay $e")
                    view?.navigateToErrorScreen()
                }
            }
        }
    }

    override fun cashOut() {
        val userAddress = tokenKeyProvider.publicKey.toBase58Instance()

        val moonpayUrl = moonpayWidgetUrlBuilder.buildSellWidgetUrl(
            tokenSymbol = Constants.SOL_SYMBOL,
            userAddress = userAddress,
            fiatSymbol = currentFiat.abbriviation,
            tokenAmountToSell = userSelectedAmount.formatTokenForMoonpay(),
        )
        view?.showMoonpayWidget(url = moonpayUrl)
    }

    override fun onTokenAmountChanged(newValue: String) {
        rawUserSelectedAmount = newValue

        val buttonState = determineButtonState()
        if (buttonState.isEnabled) {
            restartLoadSellQuoteJob()
        } else {
            sellQuoteJob?.cancel()
            view?.resetFiatAndFee(feeSymbol = currentFiat.uiSymbol)
        }
        view?.setButtonState(buttonState)
    }

    // delete or use in PWN-6284
//    override fun onCurrencyAmountChanged(newValue: String) {
//        sellQuoteJob?.cancel()
//
//        val newCurrencyAmount = newValue.toBigDecimalOrZero()
//        val newTokenAmount = newCurrencyAmount.divide(tokenPrice, 2, RoundingMode.HALF_EVEN)
//        val buttonState = determineButtonState(newTokenAmount)
//        if (buttonState.isEnabled) {
//            userSelectedAmount = newTokenAmount
//            startLoadSellQuoteJob()
//        } else {
//            view?.setTokenAndFeeValue(ZERO_STRING_VALUE)
//        }
//        view?.setButtonState(buttonState)
//    }

    override fun onUserMaxClicked() {
        view?.setTokenAmount(userSolBalance.formatTokenForMoonpay())
    }

    private fun determineButtonState(): SellPayloadContract.CashOutButtonState {
        return when {
            userSelectedAmount.isLessThan(minTokenSellAmount) -> {
                SellPayloadContract.CashOutButtonState(
                    isEnabled = false,
                    backgroundColor = R.color.bg_rain,
                    textColor = R.color.text_mountain,
                    text = resourceProvider.getString(
                        R.string.sell_min_sol_amount,
                        minTokenSellAmount.formatTokenForMoonpay()
                    )
                )
            }
            userSelectedAmount.isMoreThan(maxTokenSellAmount.orZero()) -> {
                SellPayloadContract.CashOutButtonState(
                    isEnabled = false,
                    backgroundColor = R.color.bg_rain,
                    textColor = R.color.text_mountain,
                    text = resourceProvider.getString(
                        R.string.sell_max_sol_amount,
                        maxTokenSellAmount.orZero().formatTokenForMoonpay()
                    )
                )
            }
            userSelectedAmount.isMoreThan(userSolBalance) -> {
                SellPayloadContract.CashOutButtonState(
                    isEnabled = false,
                    backgroundColor = R.color.bg_rain,
                    textColor = R.color.text_mountain,
                    text = resourceProvider.getString(R.string.sell_not_enough_sol)
                )
            }
            else -> {
                SellPayloadContract.CashOutButtonState(
                    isEnabled = true,
                    backgroundColor = R.color.bg_night,
                    textColor = R.color.text_snow,
                    text = resourceProvider.getString(R.string.common_cash_out)
                )
            }
        }
    }
}
