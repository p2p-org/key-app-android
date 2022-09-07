package org.p2p.wallet.moonpay.ui.new

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.uikit.components.FocusMode
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.BuyWithTransferFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.select.bottomsheet.SelectCurrencyBottomSheet
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.moonpay.interactor.BANK_TRANSFER_UK_CODE
import org.p2p.wallet.moonpay.interactor.MoonpayQuotesInteractor
import org.p2p.wallet.moonpay.interactor.PaymentMethodsInteractor
import org.p2p.wallet.moonpay.interactor.SEPA_BANK_TRANSFER
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.moonpay.model.Method
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.repository.MoonpayRepository
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.Constants.USD_SYMBOL
import org.p2p.wallet.utils.asCurrency
import org.p2p.wallet.utils.formatToken
import org.p2p.wallet.utils.formatUsd
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.orZero
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.toBigDecimalOrZero
import org.p2p.wallet.utils.toUsd
import timber.log.Timber
import java.math.BigDecimal

private const val DELAY_IN_MS = 500L

private val TOKENS_VALID_FOR_BUY = setOf(Constants.SOL_SYMBOL, Constants.USDC_SYMBOL)

private const val DEFAULT_FIAT_AMOUNT = 40

class NewBuyPresenter(
    tokenToBuy: Token,
    private val moonpayRepository: MoonpayRepository,
    private val minBuyErrorFormat: String,
    private val maxBuyErrorFormat: String,
    private val buyAnalytics: BuyAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val userInteractor: UserInteractor,
    private val paymentMethodsInteractor: PaymentMethodsInteractor,
    private val resourcesProvider: ResourcesProvider,
    private val moonpayQuotesInteractor: MoonpayQuotesInteractor,
    bankTransferFeatureToggle: BuyWithTransferFeatureToggle,
) : BasePresenter<NewBuyContract.View>(), NewBuyContract.Presenter {

    private lateinit var tokensToBuy: List<Token>

    private val paymentMethods = mutableListOf<PaymentMethod>()

    private var selectedCurrency: BuyCurrency.Currency = SelectCurrencyBottomSheet.DEFAULT_CURRENCY
    private var selectedToken: Token = tokenToBuy
    private lateinit var selectedPaymentMethod: PaymentMethod
    private lateinit var currentAlphaCode: String

    private var amount: String = "0"
    private var isSwappedToToken: Boolean = false
    private var currentBuyViewData: BuyViewData? = null
    private val currencySelectionEnabled = bankTransferFeatureToggle.value

    private var calculationJob: Job? = null

    private val currenciesToSelect: List<BuyCurrency.Currency> = listOf(
        BuyCurrency.Currency.create(Constants.GBP_SYMBOL),
        BuyCurrency.Currency.create(Constants.EUR_SYMBOL),
        SelectCurrencyBottomSheet.DEFAULT_CURRENCY
    )

    override fun attach(view: NewBuyContract.View) {
        super.attach(view)
        loadTokensToBuy()
        loadAvailablePaymentMethods()
    }

    private fun loadTokensToBuy() {
        launch {
            tokensToBuy = userInteractor.getTokensForBuy(TOKENS_VALID_FOR_BUY.toList())
            loadMoonpayBuyQuotes()
        }
    }

    private fun loadAvailablePaymentMethods() {
        launch {
            // show loading
            currentAlphaCode = paymentMethodsInteractor.getBankTransferAlphaCode()
            val availablePaymentMethods = paymentMethodsInteractor.getAvailablePaymentMethods(currentAlphaCode)
            selectedPaymentMethod = availablePaymentMethods.first { it.isSelected }
            paymentMethods.addAll(availablePaymentMethods)
            view?.showPaymentMethods(paymentMethods)
            validatePaymentMethod()

            preselectMinimalFiatAmount()
        }
    }

    private fun loadMoonpayBuyQuotes() {
        launch {
            val currencyCodes = currenciesToSelect.map { it.code }
            moonpayQuotesInteractor.loadQuotes(currencyCodes, tokensToBuy)
        }
    }

    private fun preselectMinimalFiatAmount() {
        view?.showPreselectedAmount(DEFAULT_FIAT_AMOUNT.toString())
        setBuyAmount(DEFAULT_FIAT_AMOUNT.toString(), isDelayEnabled = false)
    }

    override fun onBackPressed() {
        buyAnalytics.logBuyGoingBack(
            buySum = currentBuyViewData?.receiveAmount?.toBigDecimal() ?: BigDecimal.ZERO,
            buyCurrency = selectedToken.tokenSymbol,
            buyUSD = currentBuyViewData?.price ?: BigDecimal.ZERO
        )
        view?.close()
    }

    override fun onPaymentMethodSelected(selectedMethod: PaymentMethod) {
        selectedPaymentMethod = selectedMethod
        paymentMethods.forEach { paymentMethod ->
            paymentMethod.isSelected = paymentMethod.method == selectedMethod.method
        }
        validatePaymentMethod()

        view?.showPaymentMethods(paymentMethods)
    }

    private fun validatePaymentMethod() {
        if (selectedPaymentMethod.method == Method.BANK_TRANSFER) {
            if (currentAlphaCode == BANK_TRANSFER_UK_CODE) {
                selectCurrency(BuyCurrency.Currency.create(Constants.GBP_SYMBOL))
            } else {
                selectCurrency(BuyCurrency.Currency.create(Constants.EUR_SYMBOL))
            }
        }
    }

    override fun onSelectTokenClicked() {
        view?.showTokensToBuy(selectedToken, tokensToBuy)
    }

    override fun onSelectCurrencyClicked() {
        if (currencySelectionEnabled) {
            view?.showCurrency(currenciesToSelect, selectedCurrency)
        }
    }

    override fun onTotalClicked() {
        currentBuyViewData?.let {
            view?.showTotalData(it)
        }
    }

    override fun setToken(token: Token) {
        selectedToken = token
        recalculate()
    }

    private fun selectCurrency(currency: BuyCurrency.Currency) {
        view?.setCurrencyCode(currency.code)
        setCurrency(currency)
    }

    override fun setCurrency(currency: BuyCurrency.Currency) {
        selectedCurrency = currency
        if (isValidCurrencyForPay()) {
            recalculate()
        }
    }

    private fun isValidCurrencyForPay(): Boolean {
        if (selectedPaymentMethod.method == Method.BANK_TRANSFER) {
            if (selectedCurrency.code == Constants.USD_READABLE_SYMBOL) {
                paymentMethods.find { it.method == Method.CARD }?.let {
                    onPaymentMethodSelected(it)
                }
                return false
            } else if (selectedCurrency.code == Constants.GBP_SYMBOL && currentAlphaCode != BANK_TRANSFER_UK_CODE) {
                view?.setContinueButtonEnabled(false)
                view?.showMessage(resourcesProvider.getString(R.string.buy_gbp_error))
                return false
            }
        }
        return true
    }

    override fun onFocusModeChanged(focusMode: FocusMode) {
        isSwappedToToken = focusMode == FocusMode.TOKEN
    }

    override fun setBuyAmount(amount: String, isDelayEnabled: Boolean) {
        this.amount = amount
        view?.setContinueButtonEnabled(false)
        if (isValidCurrencyForPay()) {
            calculateTokens(amount, isDelayEnabled)
        }
    }

    private fun recalculate() {
        view?.setContinueButtonEnabled(false)
        calculateTokens(amount, isDelayEnabled = false)
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
                baseCurrencyCode = baseCurrencyCode,
                paymentMethod = selectedPaymentMethod.paymentType
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
            is MoonpayBuyResult.MinimumAmountError -> {
                buyResultAnalytics = BuyAnalytics.BuyResult.ERROR
                view?.apply {
                    setContinueButtonEnabled(false)
                    showMessage(minBuyErrorFormat.format(selectedCurrency.code.symbolFromCode()))
                    clearOppositeFieldAndTotal("${selectedCurrency.code.symbolFromCode()} 0")
                }
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
        val currency = if (currencySymbol == Constants.USD_READABLE_SYMBOL) USD_SYMBOL else currencySymbol
        val data = BuyViewData(
            tokenSymbol = selectedToken.tokenSymbol,
            currencySymbol = currency,
            price = buyCurrencyInfo.price.scaleShort(),
            receiveAmount = buyCurrencyInfo.receiveAmount,
            processingFee = buyCurrencyInfo.feeAmount.scaleShort(),
            networkFee = buyCurrencyInfo.networkFeeAmount.scaleShort(),
            extraFee = buyCurrencyInfo.extraFeeAmount.scaleShort(),
            accountCreationCost = null,
            total = buyCurrencyInfo.totalAmount.scaleShort(),
            receiveAmountText = amount,
            purchaseCostText = if (isSwappedToToken) {
                currencyForTokensAmount.asCurrency(currency)
            } else {
                buyCurrencyInfo.receiveAmount.toBigDecimal().asCurrency(currency)
            }
        )
        view?.apply {
            showTotal(data)
            currentBuyViewData = data
            showMessage(message = null, selectedToken.tokenSymbol)
            setContinueButtonEnabled(true)
        }
    }

    private fun handleEnteredAmountInvalid(loadedBuyCurrency: BuyCurrency.Currency) {
        val isAmountLower = amount.toBigDecimal() < loadedBuyCurrency.minAmount
        val errorMessageRaw = if (isAmountLower) minBuyErrorFormat else maxBuyErrorFormat
        val maxAmount = loadedBuyCurrency.maxAmount
        val symbol = selectedCurrency.code.symbolFromCode()
        val message = if (isAmountLower) {
            minBuyErrorFormat.format(symbol)
        } else {
            maxBuyErrorFormat.format("$symbol $maxAmount")
        }
        view?.apply {
            setContinueButtonEnabled(false)
            showMessage(message)
            clearOppositeFieldAndTotal("${selectedCurrency.code.symbolFromCode()} 0")
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
            total = BigDecimal.ZERO,
            receiveAmountText = null
        )
        view?.apply {
            showTotal(clearedData)
            setContinueButtonEnabled(false)
            showMessage(null, selectedToken.tokenSymbol)
        }
    }

    override fun onContinueClicked() {
        currentBuyViewData?.let {
            val paymentType = getValidPaymentType()
            view?.navigateToMoonpay(
                amount = it.total.toString(),
                selectedToken,
                selectedCurrency,
                paymentType
            )
            // TODO append analytics with selected token and currency
            buyAnalytics.logBuyContinuing(
                buyCurrency = it.tokenSymbol,
                buySum = it.price,
                buyProvider = "moonpay",
                buyUSD = it.price.toUsd(it.price).orZero(),
                lastScreenName = analyticsInteractor.getPreviousScreenName()
            )
        }
    }

    fun getValidPaymentType(): String {
        return if (currentAlphaCode == BANK_TRANSFER_UK_CODE && selectedCurrency.code == Constants.EUR_SYMBOL) {
            SEPA_BANK_TRANSFER
        } else {
            selectedPaymentMethod.paymentType
        }
    }

    private fun String.symbolFromCode() = when (this.lowercase()) {
        "eur" -> "€"
        "usd" -> "$"
        "gbp" -> "£"
        else -> throw IllegalArgumentException("Unknown currency $this")
    }

    override fun detach() {
        calculationJob?.cancel()
        super.detach()
    }
}
