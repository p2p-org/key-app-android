package org.p2p.wallet.sell.ui.payload

import org.p2p.core.utils.Constants
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShortOrFirstNotZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.moonpay.model.MoonpayWidgetUrlBuilder
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellFiatCurrency
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.sell.ui.payload.SellPayloadContract.ViewState
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val SELL_QUOTE_REQUEST_DEBOUNCE_TIME = 10_000L
private const val ZERO_STRING_VALUE = "0"

class SellPayloadPresenter(
    private val sellInteractor: SellInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val moonpayWidgetUrlBuilder: MoonpayWidgetUrlBuilder,
    private val userInteractor: UserInteractor,
    private val resourceProvider: ResourcesProvider
) : BasePresenter<SellPayloadContract.View>(),
    SellPayloadContract.Presenter {

    private var userBalance: BigDecimal = BigDecimal.ZERO
    private var minSellAmount: BigDecimal = BigDecimal.ZERO
    private var maxSellAmount: BigDecimal? = null
    private var userSelectedAmount: BigDecimal = BigDecimal.ZERO
    private var fiat: MoonpaySellFiatCurrency? = null
    private var tokenPrice: BigDecimal = BigDecimal.ZERO
    private var sellQuoteJob: Job? = null

    override fun attach(view: SellPayloadContract.View) {
        super.attach(view)
        launch {
            try {
                view.showLoading(isVisible = true)
                checkForSellLock()
                userBalance = userInteractor.getUserSolToken()?.total.orZero()
                loadCurrencies()
                checkForMinAmount()
                startLoadSellQuoteJob()
            } catch (e: Throwable) {
                Timber.e(e, "Error on loading data from Moonpay")
                view.showErrorScreen()
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
                solAmount = amounts.tokenAmount,
                usdAmount = amounts.usdAmount.toPlainString(),
                moonpayAddress = tokenKeyProvider.publicKey.toBase58Instance()
            )
        }
    }

    private suspend fun getUserTransactionInProcess(): MoonpaySellTransaction? {
        val userTransactions = sellInteractor.loadUserSellTransactions()
        return userTransactions.find { it.status == MoonpaySellTransaction.TransactionStatus.WAITING_FOR_DEPOSIT }
    }

    private suspend fun loadCurrencies() {
        val solCurrency = sellInteractor.getAllCurrencies().firstOrNull { it.isSol() }
        minSellAmount = solCurrency?.amounts?.minSellAmount.orZero()
        maxSellAmount = solCurrency?.amounts?.maxSellAmount
        userSelectedAmount = minSellAmount
        fiat = sellInteractor.getMoonpaySellFiatCurrency()
    }

    private fun checkForMinAmount() {
        if (userBalance < minSellAmount) {
            view?.showNotEnoughMoney(minSellAmount)
        }
    }

    private fun startLoadSellQuoteJob() {
        sellQuoteJob?.cancel()
        sellQuoteJob = launch {
            while (isActive) {
                try {
                    val selectedFiat = fiat ?: error("Fiat cannot be null")
                    val sellQuote = sellInteractor.getSellQuoteForSol(userSelectedAmount, selectedFiat)

                    tokenPrice = sellQuote.tokenPrice

                    val moonpayFee = sellQuote.feeAmount
                    val fiat = "${tokenPrice}${selectedFiat.uiSymbol}"

                    val viewState = ViewState(
                        quoteAmount = sellQuote.fiatEarning.formatToken(),
                        fee = moonpayFee.scaleShortOrFirstNotZero().toString(),
                        fiat = fiat,
                        solToSell = userSelectedAmount.toString(),
                        tokenSymbol = Constants.SOL_SYMBOL,
                        fiatSymbol = selectedFiat.uiSymbol,
                        userBalance = userBalance.toString()
                    )

                    view?.updateViewState(viewState)
                    delay(SELL_QUOTE_REQUEST_DEBOUNCE_TIME)
                } catch (e: CancellationException) {
                    Timber.i(e)
                } catch (e: Throwable) {
                    Timber.e("Error on loading data from Moonpay $e")
                    view?.showErrorScreen()
                }
            }
        }
    }

    override fun cashOut() {
        val userAddress = tokenKeyProvider.publicKey.toBase58Instance()

        val moonpayUrl = moonpayWidgetUrlBuilder.buildSellWidgetUrl(
            tokenSymbol = Constants.SOL_SYMBOL,
            userAddress = userAddress,
            fiatSymbol = fiat?.symbol.orEmpty(),
            tokenAmountToSell = userSelectedAmount.toString(),
        )
        view?.showMoonpayWidget(url = moonpayUrl)
    }

    override fun onTokenAmountChanged(newValue: String) {
        val newTokenValue = newValue.toBigDecimalOrZero()
        sellQuoteJob?.cancel()

        val buttonState = determineButtonState(newTokenValue)
        if (buttonState.isEnabled) {
            userSelectedAmount = newTokenValue
            startLoadSellQuoteJob()
        } else {
            view?.setFiatAndFeeValue(ZERO_STRING_VALUE)
        }
        view?.setButtonState(buttonState)
    }

    override fun onCurrencyAmountChanged(newValue: String) {
        sellQuoteJob?.cancel()

        val newCurrencyAmount = newValue.toBigDecimalOrZero()
        val newTokenAmount = newCurrencyAmount.divide(tokenPrice, 2, RoundingMode.HALF_EVEN)
        val buttonState = determineButtonState(newTokenAmount)
        if (buttonState.isEnabled) {
            userSelectedAmount = newTokenAmount
            startLoadSellQuoteJob()
        } else {
            view?.setTokenAndFeeValue(ZERO_STRING_VALUE)
        }
        view?.setButtonState(buttonState)
    }

    override fun onUserMaxClicked() {
        view?.setTokenAmount(userBalance.toString())
    }

    private fun determineButtonState(selectedTokenAmount: BigDecimal): SellPayloadContract.CashOutButtonState {
        return when {
            selectedTokenAmount.isLessThan(minSellAmount) -> {
                SellPayloadContract.CashOutButtonState(
                    isEnabled = false,
                    backgroundColor = R.color.bg_rain,
                    textColor = R.color.text_mountain,
                    text = resourceProvider.getString(R.string.sell_min_sol_amount, minSellAmount.toString())
                )
            }
            selectedTokenAmount.isMoreThan(maxSellAmount.orZero()) -> {
                SellPayloadContract.CashOutButtonState(
                    isEnabled = false,
                    backgroundColor = R.color.bg_rain,
                    textColor = R.color.text_mountain,
                    text = resourceProvider.getString(R.string.sell_max_sol_amount, maxSellAmount.toString())
                )
            }
            selectedTokenAmount.isMoreThan(userBalance) -> {
                SellPayloadContract.CashOutButtonState(
                    isEnabled = false,
                    backgroundColor = R.color.bg_rain,
                    textColor = R.color.text_mountain,
                    text = resourceProvider.getString(R.string.sell_not_enough_sol)
                )
            }
            else -> {
                userSelectedAmount = selectedTokenAmount
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
