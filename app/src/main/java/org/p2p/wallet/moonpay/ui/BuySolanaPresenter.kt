package org.p2p.wallet.moonpay.ui

import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.Constants.USD_SYMBOL
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toUsd
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.moonpay.interactor.CREDIT_DEBIT_CARD
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.repository.buy.MoonpayBuyRepository
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DELAY_IN_MS = 500L

class BuySolanaPresenter(
    private val tokenToBuy: Token,
    private val moonpayBuyRepository: MoonpayBuyRepository,
    private val minBuyErrorFormat: String,
    private val maxBuyErrorFormat: String,
    private val networkServicesUrlProvider: NetworkServicesUrlProvider,
    private val buyAnalytics: BuyAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
) : BasePresenter<BuySolanaContract.View>(), BuySolanaContract.Presenter {

    private var calculationJob: Job? = null

    private var amount: String = "0"

    private var currentBuyViewData: BuyViewData? = null

    private var isSwappedToToken: Boolean = false

    private var prefix = USD_SYMBOL

    override fun loadData() {
        launch {
            try {
                view?.showLoading(isLoading = true)
                val price = moonpayBuyRepository.getCurrencyAskPrice(tokenToBuy).scaleShort()
                view?.showTokenPrice("$USD_SYMBOL$price")
                if (isSwappedToToken) {
                    updateViewWithData()
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error loading currency ask price")
                view?.showErrorMessage(e)
            } finally {
                val prevScreenName =
                    if (analyticsInteractor.getPreviousScreenName() == ScreenNames.Token.TOKEN_SCREEN) {
                        ScreenNames.Token.TOKEN_SCREEN
                    } else {
                        ScreenNames.Main.MAIN
                    }
                buyAnalytics.logScreenOpened(lastScreenName = prevScreenName)
                view?.showLoading(false)
            }
        }
    }

    override fun onContinueClicked() {
        currentBuyViewData?.let {
            view?.navigateToMoonpay(amount = it.total.toString())
            buyAnalytics.logBuyContinuing(
                buyCurrency = it.tokenSymbol,
                buySum = it.price,
                buyProvider = "moonpay",
                buyUSD = it.price.toUsd(it.price).orZero(),
                lastScreenName = analyticsInteractor.getPreviousScreenName()
            )
        }
    }

    override fun onBackPressed() {
        buyAnalytics.logBuyGoingBack(
            buySum = currentBuyViewData?.receiveAmount?.toBigDecimal() ?: BigDecimal.ZERO,
            buyCurrency = tokenToBuy.tokenSymbol,
            buyUSD = currentBuyViewData?.price ?: BigDecimal.ZERO
        )
        view?.close()
    }

    override fun onSwapClicked() {
        isSwappedToToken = !isSwappedToToken
        updateViewWithData()
    }

    override fun setBuyAmount(amount: String, isDelayEnabled: Boolean) {
        this.amount = amount
        view?.setContinueButtonEnabled(false)
        calculateTokens(amount, isDelayEnabled)
    }

    private fun updateViewWithData() {
        prefix = if (isSwappedToToken) tokenToBuy.tokenSymbol else USD_SYMBOL
        view?.swapData(isSwappedToToken, prefix)

        setBuyAmount(amount, isDelayEnabled = false)
    }

    private fun calculateTokens(amount: String, isDelayEnabled: Boolean) {
        calculationJob?.cancel()

        val parsedAmount = amount.toBigDecimalOrZero()
        if (amount.isBlank() || parsedAmount.isZero()) {
            clear()
            return
        }
        calculationJob = calculateBuyDataWithMoonpay(isDelayEnabled)
    }

    private fun calculateBuyDataWithMoonpay(isDelayEnabled: Boolean): Job = launch {
        val amountInTokens: String?
        val amountInCurrency: String?
        if (isSwappedToToken) {
            amountInTokens = amount
            amountInCurrency = null
        } else {
            amountInTokens = null
            amountInCurrency = amount
        }

        try {
            if (isDelayEnabled) delay(DELAY_IN_MS)

            view?.showLoading(isLoading = true)

            val baseCurrencyCode = USD_READABLE_SYMBOL.lowercase()
            val result = moonpayBuyRepository.getBuyCurrencyData(
                baseCurrencyAmount = amountInCurrency,
                quoteCurrencyAmount = amountInTokens,
                tokenToBuy = tokenToBuy,
                baseCurrencyCode = baseCurrencyCode,
                paymentMethod = CREDIT_DEBIT_CARD
            )
            onBuyCurrencyLoadSuccess(result)
        } catch (error: Throwable) {
            onBuyCurrencyLoadFailed(error)
        } finally {
            view?.showLoading(isLoading = false)
        }
    }

    private fun onBuyCurrencyLoadSuccess(buyResult: MoonpayBuyResult) {
        val buyResultAnalytics: BuyAnalytics.BuyResult
        Timber.d(buyResult.toString())
        when (buyResult) {
            is MoonpayBuyResult.Success -> {
                buyResultAnalytics = BuyAnalytics.BuyResult.SUCCESS
                updateViewWithBuyCurrencyData(buyResult.data)
            }
            is MoonpayBuyResult.Error -> {
                buyResultAnalytics = BuyAnalytics.BuyResult.ERROR
                view?.showMessage(buyResult.message)
            }
            is MoonpayBuyResult.MinAmountError -> {
                // May by only in case of new buy
                error("MinAmountError may be only in case of new buy screen")
            }
            is MoonpayBuyResult.MaxAmountError -> {
                // May by only in case of new buy
                error("MaxAmountError may be only in case of new buy screen")
            }
        }
        buyAnalytics.logBuyPaymentResultShown(buyResultAnalytics)
    }

    private fun onBuyCurrencyLoadFailed(error: Throwable) {
        if (error is CancellationException) {
            Timber.w("Cancelled get currency request")
        } else {
            Timber.e(error, "Error loading buy currency data")
            view?.showErrorMessage(error)
        }
    }

    private fun updateViewWithBuyCurrencyData(buyCurrencyInfo: BuyCurrency) {
        val enteredAmountBigDecimal = amount.toBigDecimal()
        val loadedBuyCurrency = if (isSwappedToToken) buyCurrencyInfo.quoteCurrency else buyCurrencyInfo.baseCurrency
        val enteredAmountLowerThanMax = loadedBuyCurrency.maxAmount
            ?.let { maxCurrencyAmount -> enteredAmountBigDecimal <= maxCurrencyAmount }
            ?: true
        val enteredAmountHigherThanMin = enteredAmountBigDecimal >= loadedBuyCurrency.minAmount

        if (enteredAmountHigherThanMin && enteredAmountLowerThanMax) {
            handleEnteredAmountValid(buyCurrencyInfo)
        } else {
            handleEnteredAmountInvalid(loadedBuyCurrency)
        }
    }

    private fun handleEnteredAmountValid(buyCurrencyInfo: BuyCurrency) {
        val receiveSymbol = if (isSwappedToToken) USD_SYMBOL else tokenToBuy.tokenSymbol
        val amount = if (isSwappedToToken) {
            buyCurrencyInfo.totalAmount.formatFiat()
        } else {
            buyCurrencyInfo.receiveAmount.toBigDecimal().formatToken()
        }
        val currencyForTokensAmount = buyCurrencyInfo.price * buyCurrencyInfo.receiveAmount.toBigDecimal()
        val data = BuyViewData(
            tokenSymbol = tokenToBuy.tokenSymbol,
            currencySymbol = USD_SYMBOL,
            price = buyCurrencyInfo.price.scaleShort(),
            receiveAmount = buyCurrencyInfo.receiveAmount,
            processingFee = buyCurrencyInfo.feeAmount.scaleShort(),
            networkFee = buyCurrencyInfo.networkFeeAmount.scaleShort(),
            extraFee = buyCurrencyInfo.extraFeeAmount.scaleShort(),
            accountCreationCost = null,
            total = buyCurrencyInfo.totalAmount.scaleShort(),
            receiveAmountText = "$amount $receiveSymbol",
            purchaseCostText = if (isSwappedToToken) currencyForTokensAmount.asUsd() else null
        )
        view?.apply {
            showData(data)
            currentBuyViewData = data
            showMessage(message = null)
            setContinueButtonEnabled(true)
        }
    }

    private fun handleEnteredAmountInvalid(loadedBuyCurrency: BuyCurrency.Currency) {
        val isCurrencyUsd = loadedBuyCurrency.code == USD_READABLE_SYMBOL.lowercase()
        val suffixPrefix = if (isCurrencyUsd) {
            USD_SYMBOL
        } else {
            loadedBuyCurrency.code.uppercase()
        }
        val isAmountLower = amount.toBigDecimal() < loadedBuyCurrency.minAmount

        val amountForFormatter = if (isAmountLower) loadedBuyCurrency.minAmount else loadedBuyCurrency.maxAmount
        val suffixPrefixWithAmount =
            if (isCurrencyUsd) {
                "$suffixPrefix$amountForFormatter"
            } else {
                "$amountForFormatter $suffixPrefix"
            }

        val errorMessageRaw = if (isAmountLower) minBuyErrorFormat else maxBuyErrorFormat
        view?.apply {
            setContinueButtonEnabled(false)
            showMessage(
                errorMessageRaw.format(suffixPrefixWithAmount)
            )
        }
    }

    private fun clear() {
        val data = currentBuyViewData ?: return
        val clearedData = data.copy(
            receiveAmount = 0.0,
            processingFee = BigDecimal.ZERO,
            networkFee = BigDecimal.ZERO,
            extraFee = BigDecimal.ZERO,
            accountCreationCost = null,
            total = BigDecimal.ZERO
        )
        view?.apply {
            showData(clearedData)
            setContinueButtonEnabled(false)
            showMessage(null)
        }
    }

    override fun detach() {
        calculationJob?.cancel()
        super.detach()
    }
}
