package org.p2p.wallet.moonpay.ui.new

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.uikit.components.FocusMode
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.select.bottomsheet.SelectCurrencyBottomSheet
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.moonpay.interactor.PaymentMethodsInteractor
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.repository.MoonpayRepository
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.asCurrency
import org.p2p.wallet.utils.formatToken
import org.p2p.wallet.utils.formatUsd
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.toBigDecimalOrZero
import timber.log.Timber
import java.math.BigDecimal

private const val DELAY_IN_MS = 500L

private val TOKENS_VALID_FOR_BUY = setOf(Constants.SOL_SYMBOL, Constants.USDC_SYMBOL)

class NewBuyPresenter(
    private val tokenToBuy: Token,
    private val moonpayRepository: MoonpayRepository,
    private val minBuyErrorFormat: String,
    private val maxBuyErrorFormat: String,
    private val buyAnalytics: BuyAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val userInteractor: UserInteractor,
    private val paymentMethodsInteractor: PaymentMethodsInteractor
) : BasePresenter<NewBuyContract.View>(), NewBuyContract.Presenter {

    private lateinit var tokensToBuy: List<Token>

    private val paymentMethods = mutableListOf<PaymentMethod>()

    private var selectedCurrency: BuyCurrency.Currency = SelectCurrencyBottomSheet.DEFAULT_CURRENCY
    private var selectedToken: Token = tokenToBuy

    private var amount: String = "0"
    private var isSwappedToToken: Boolean = false
    private var currentBuyViewData: BuyViewData? = null

    private var calculationJob: Job? = null

    override fun attach(view: NewBuyContract.View) {
        super.attach(view)
        loadTokensToBuy()
        loadAvailablePaymentMethods()
    }

    private fun loadTokensToBuy() {
        launch {
            tokensToBuy = userInteractor.getTokensForBuy(TOKENS_VALID_FOR_BUY.toList())
        }
    }

    private fun loadAvailablePaymentMethods() {
        launch {
            // show loading
            paymentMethods.addAll(paymentMethodsInteractor.getAvailablePaymentMethods())
            view?.showPaymentMethods(paymentMethods)
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

    override fun onPaymentMethodSelected(selectedMethod: PaymentMethod) {
        paymentMethods.forEach { paymentMethod ->
            paymentMethod.isSelected = paymentMethod.method == selectedMethod.method
        }

        view?.showPaymentMethods(paymentMethods)
    }

    override fun onSelectTokenClicked() {
        view?.showTokensToBuy(selectedToken, tokensToBuy)
    }

    override fun onSelectCurrencyClicked() {
        view?.showCurrency(selectedCurrency)
    }

    override fun onTotalClicked() {
        currentBuyViewData?.let {
            view?.showTotalData(it)
        }
    }

    override fun setToken(token: Token) {
        selectedToken = token
    }

    override fun setCurrency(currency: BuyCurrency.Currency) {
        selectedCurrency = currency
    }

    override fun onFocusModeChanged(focusMode: FocusMode) {
        isSwappedToToken = focusMode == FocusMode.TOKEN
    }

    override fun setBuyAmount(amount: String, isDelayEnabled: Boolean) {
        this.amount = amount
        view?.setContinueButtonEnabled(false)
        calculateTokens(amount, isDelayEnabled)
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

            val baseCurrencyCode = selectedCurrency.code.lowercase()
            val result = moonpayRepository.getBuyCurrencyData(
                baseCurrencyAmount = amountInCurrency,
                quoteCurrencyAmount = amountInTokens,
                tokenToBuy = selectedToken,
                baseCurrencyCode = baseCurrencyCode
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
        val receiveSymbol = if (isSwappedToToken) selectedCurrency.code else selectedToken.tokenSymbol
        val amount = if (isSwappedToToken) {
            buyCurrencyInfo.totalAmount.formatUsd()
        } else {
            buyCurrencyInfo.receiveAmount.toBigDecimal().formatToken()
        }
        val currencyForTokensAmount = buyCurrencyInfo.price * buyCurrencyInfo.receiveAmount.toBigDecimal()
        val currencySymbol = selectedCurrency.code
        val currency = if (currencySymbol == Constants.USD_READABLE_SYMBOL) "$" else currencySymbol
        val data = BuyViewData(
            tokenSymbol = tokenToBuy.tokenSymbol,
            currencySymbol = currency,
            price = buyCurrencyInfo.price.scaleShort(),
            receiveAmount = buyCurrencyInfo.receiveAmount,
            processingFee = buyCurrencyInfo.feeAmount.scaleShort(),
            networkFee = buyCurrencyInfo.networkFeeAmount.scaleShort(),
            extraFee = buyCurrencyInfo.extraFeeAmount.scaleShort(),
            accountCreationCost = null,
            total = buyCurrencyInfo.totalAmount.scaleShort(),
            receiveAmountText = "$amount $receiveSymbol",
            purchaseCostText = if (isSwappedToToken) currencyForTokensAmount.asCurrency(currency) else null
        )
        view?.apply {
            showTotal(data)
            currentBuyViewData = data
            showMessage(message = null)
            setContinueButtonEnabled(true)
        }
    }

    private fun handleEnteredAmountInvalid(loadedBuyCurrency: BuyCurrency.Currency) {
        val isCurrencyUsd = loadedBuyCurrency.code == Constants.USD_READABLE_SYMBOL.lowercase()
        val suffixPrefix = if (isCurrencyUsd) {
            selectedCurrency.code
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
            showTotal(clearedData)
            setContinueButtonEnabled(false)
            showMessage(null)
        }
    }

    override fun onContinueClicked() {
        // TODO("Not yet implemented")
    }

    override fun detach() {
        calculationJob?.cancel()
        super.detach()
    }
}
