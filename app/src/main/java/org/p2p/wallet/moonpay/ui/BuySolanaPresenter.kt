package org.p2p.wallet.moonpay.ui

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.BuyData
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.repository.MoonpayRepository
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.wallet.utils.Constants.USD_SYMBOL
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.orZero
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.toBigDecimalOrZero
import org.p2p.wallet.utils.toUsd
import timber.log.Timber
import java.math.BigDecimal

private const val DELAY_IN_MS = 500L

class BuySolanaPresenter(
    private val token: Token,
    private val moonpayRepository: MoonpayRepository,
    private val minBuyErrorFormat: String,
    private val maxBuyErrorFormat: String,
    private val buyAnalytics: BuyAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor
) : BasePresenter<BuySolanaContract.View>(), BuySolanaContract.Presenter {

    private var calculationJob: Job? = null

    private var amount: String = "0"

    private var data: BuyData? = null

    private var isSwapped: Boolean = false

    private var prefix = USD_SYMBOL

    override fun loadData() {
        launch {
            try {
                view?.showLoading(true)
                val price = moonpayRepository.getCurrencyAskPrice(token.tokenSymbol.lowercase()).scaleShort()
                view?.showTokenPrice("$USD_SYMBOL$price")
                if (isSwapped) {
                    updateViewWithData()
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error loading currency ask price")
                view?.showErrorMessage(e)
            } finally {
                buyAnalytics.logBuyViewed()
                view?.showLoading(false)
            }
        }
    }

    override fun onContinueClicked() {
        data?.let {
            view?.navigateToMoonpay(it.total.toString())
            // TODO resolve [buyProvider]
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
            buySum = data?.receiveAmount?.toBigDecimal() ?: BigDecimal.ZERO,
            buyCurrency = token.tokenSymbol,
            buyUSD = data?.price ?: BigDecimal.ZERO
        )
        view?.close()
    }

    override fun onSwapClicked() {
        isSwapped = !isSwapped
        updateViewWithData()
    }

    override fun setBuyAmount(amount: String, isDelayEnabled: Boolean) {
        this.amount = amount
        calculateTokens(amount, isDelayEnabled)
    }

    private fun updateViewWithData() {
        prefix = if (isSwapped) token.tokenSymbol else USD_SYMBOL
        view?.swapData(isSwapped, prefix)
        setBuyAmount(amount, isDelayEnabled = false)
    }

    private fun calculateTokens(amount: String, isDelayEnabled: Boolean) {
        calculationJob?.cancel()

        val parsedAmount = amount.toBigDecimalOrZero()
        if (amount.isBlank() || parsedAmount.isZero()) {
            clear()
            return
        }

        val amountInTokens: String?
        val amountInCurrency: String?
        if (isSwapped) {
            amountInTokens = amount
            amountInCurrency = null
        } else {
            amountInTokens = null
            amountInCurrency = amount
        }

        calculationJob = launch {
            try {
                if (isDelayEnabled) delay(DELAY_IN_MS)
                view?.showLoading(true)
                val baseCurrencyCode = USD_READABLE_SYMBOL.lowercase()
                val buyResult: BuyAnalytics.BuyResult
                val result = moonpayRepository.getCurrency(
                    baseCurrencyAmount = amountInCurrency,
                    quoteCurrencyAmount = amountInTokens,
                    quoteCurrencyCode = token.getTokenSymbolForMoonPay(),
                    baseCurrencyCode = baseCurrencyCode
                )
                when (result) {
                    is MoonpayBuyResult.Success -> {
                        buyResult = BuyAnalytics.BuyResult.SUCCESS
                        handleSuccess(result.data)
                    }
                    is MoonpayBuyResult.Error -> {
                        buyResult = BuyAnalytics.BuyResult.ERROR
                        view?.showMessage(result.message)
                    }
                }
                buyAnalytics.logBuyPaymentResultShown(buyResult)
            } catch (e: CancellationException) {
                Timber.w("Cancelled get currency request")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading buy currency data")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun handleSuccess(info: BuyCurrency) {
        val amountBigDecimal = amount.toBigDecimal()
        val currency = if (isSwapped) info.quoteCurrency else info.baseCurrency
        if (amountBigDecimal >= currency.minAmount && currency.maxAmount?.let { amountBigDecimal <= it } != false) {
            val receiveSymbol = if (isSwapped) USD_SYMBOL else token.tokenSymbol
            val amount = if (isSwapped) info.totalAmount.scaleShort() else info.receiveAmount
            val currencyForTokensAmount = info.price * info.receiveAmount.toBigDecimal()
            val data = BuyData(
                tokenSymbol = token.tokenSymbol,
                currencySymbol = USD_SYMBOL,
                price = info.price.scaleShort(),
                receiveAmount = info.receiveAmount,
                processingFee = info.feeAmount.scaleShort(),
                networkFee = info.networkFeeAmount.scaleShort(),
                extraFee = info.extraFeeAmount.scaleShort(),
                accountCreationCost = null,
                total = info.totalAmount.scaleShort(),
                receiveAmountText = "$amount $receiveSymbol",
                purchaseCostText = if (isSwapped) currencyForTokensAmount.scaleShort().toString() else null
            )
            view?.showData(data).also { this.data = data }
            view?.showMessage(null)
        } else {
            val isUsd = currency.code == USD_READABLE_SYMBOL.lowercase()
            val suffixPrefix = if (isUsd) USD_SYMBOL else currency.code.uppercase()
            val amountIsLower = amountBigDecimal < currency.minAmount
            val amountForFormatter = if (amountIsLower) {
                currency.minAmount
            } else {
                currency.maxAmount
            }
            val suffixPrefixWithAmount =
                if (isUsd) {
                    "$suffixPrefix$amountForFormatter"
                } else {
                    "$amountForFormatter $suffixPrefix"
                }
            view?.showMessage(
                if (amountIsLower) {
                    minBuyErrorFormat
                } else {
                    maxBuyErrorFormat
                }.format(suffixPrefixWithAmount)
            )
        }
    }

    private fun clear() {
        val data = data ?: return
        val clearedData = data.copy(
            receiveAmount = 0.0,
            processingFee = BigDecimal.ZERO,
            networkFee = BigDecimal.ZERO,
            extraFee = BigDecimal.ZERO,
            accountCreationCost = null,
            total = BigDecimal.ZERO
        )
        view?.showData(clearedData)
        view?.showMessage(null)
    }

    private fun Token.getTokenSymbolForMoonPay(): String {
        val tokenLowercase = token.tokenSymbol.lowercase()
        return if (token.isUSDC) {
            "${tokenLowercase}_${SOL_SYMBOL.lowercase()}"
        } else {
            tokenLowercase
        }
    }
}
