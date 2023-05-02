package org.p2p.wallet.moonpay.ui.new

import android.content.res.Resources
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.Constants.USD_SYMBOL
import org.p2p.core.utils.asCurrency
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleShort
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.uikit.components.FocusField
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.BuyWithTransferFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.ui.select.bottomsheet.SelectCurrencyBottomSheet
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.moonpay.interactor.BANK_TRANSFER_UK_CODE
import org.p2p.wallet.moonpay.interactor.BuyInteractor
import org.p2p.wallet.moonpay.interactor.BuyInteractor.Companion.DEFAULT_MIN_BUY_CURRENCY_AMOUNT
import org.p2p.wallet.moonpay.interactor.PaymentMethodsInteractor
import org.p2p.wallet.moonpay.interactor.SEPA_BANK_TRANSFER
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.BuyDetailsState
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.emptyString

private const val DELAY_IN_MS = 500L

class NewBuyPresenter(
    tokenToBuy: Token,
    private val fiatToken: String? = null,
    private val fiatAmount: String? = null,
    private val buyAnalytics: BuyAnalytics,
    private val userInteractor: UserInteractor,
    private val paymentMethodsInteractor: PaymentMethodsInteractor,
    private val resources: Resources,
    private val buyInteractor: BuyInteractor,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    bankTransferFeatureToggle: BuyWithTransferFeatureToggle,
) : BasePresenter<NewBuyContract.View>(), NewBuyContract.Presenter {

    private lateinit var tokensToBuy: List<Token>

    private val paymentMethods = mutableListOf<PaymentMethod>()

    private var selectedCurrency: BuyCurrency.Currency = SelectCurrencyBottomSheet.DEFAULT_CURRENCY
    private var selectedToken: Token = tokenToBuy
    private var selectedPaymentMethod: PaymentMethod? = null
    private var currentAlphaCode: String = emptyString()

    private var amount: String = "0"
    private var isSwappedToToken: Boolean = false
    private var buyDetailsState: BuyDetailsState? = null
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
        view.setCurrencySelectionIsEnabled(currencySelectionEnabled)
        loadTokensToBuy()
        loadAvailablePaymentMethods()
        val prevScreenName =
            if (analyticsInteractor.getCurrentScreenName() == ScreenNames.Token.TOKEN_SCREEN) {
                ScreenNames.Token.TOKEN_SCREEN
            } else {
                ScreenNames.Main.MAIN
            }
        buyAnalytics.logScreenOpened(lastScreenName = prevScreenName)
    }

    private fun loadTokensToBuy() {
        launch {
            tokensToBuy = userInteractor.getTokensForBuy()
            if (tokensToBuy.isEmpty()) {
                // cannot be empty, buy we are handling
                Timber.e(java.lang.IllegalStateException("Tokens to buy return an empty list, closing buy screen"))
                view?.close()
                return@launch
            }
            loadMoonpayBuyQuotes()
        }
    }

    private fun loadAvailablePaymentMethods() {
        launch {
            view?.showLoading(isLoading = true)
            currentAlphaCode = paymentMethodsInteractor.getBankTransferAlphaCode()

            val availablePaymentMethods = paymentMethodsInteractor.getAvailablePaymentMethods(currentAlphaCode)
            selectedPaymentMethod = availablePaymentMethods.first { it.isSelected }
            paymentMethods.addAll(availablePaymentMethods)

            if (paymentMethods.size > 1) {
                view?.showPaymentMethods(paymentMethods)
            } else {
                view?.showPaymentMethods(null)
            }

            validatePaymentMethod()

            if (!fiatToken.isNullOrBlank()) {
                currenciesToSelect.firstOrNull { it.code.lowercase() == fiatToken.lowercase() }?.let {
                    selectCurrency(it)
                }
            }

            if (fiatAmount != null && fiatAmount.toBigDecimalOrNull() != null) {
                selectMinimalFiatAmount(fiatAmount)
            } else {
                preselectMinimalFiatAmount()
            }
        }
    }

    private fun loadMoonpayBuyQuotes() {
        launch {
            val currencyCodes = currenciesToSelect.map { it.code }
            buyInteractor.loadQuotes(currencyCodes, tokensToBuy)
        }
    }

    private fun selectMinimalFiatAmount(amount: String) {
        view?.showPreselectedAmount(amount)
        setBuyAmount(amount, isDelayEnabled = false)
    }

    private fun preselectMinimalFiatAmount() {
        selectMinimalFiatAmount(DEFAULT_MIN_BUY_CURRENCY_AMOUNT.toString())
    }

    override fun onBackPressed() {
        buyAnalytics.logBuyGoingBack(
            buySum = currentBuyViewData?.receiveAmount?.toBigDecimal() ?: BigDecimal.ZERO,
            buyCurrency = selectedToken.tokenSymbol,
            buyUSD = currentBuyViewData?.price ?: BigDecimal.ZERO
        )
        view?.close()
    }

    override fun onPaymentMethodSelected(selectedMethod: PaymentMethod, byUser: Boolean) {
        if (byUser) {
            buyAnalytics.logBuyMethodPaymentChanged(selectedMethod)
        }
        selectedPaymentMethod = selectedMethod
        paymentMethods.forEach { paymentMethod ->
            paymentMethod.isSelected = paymentMethod.method == selectedMethod.method
        }
        validatePaymentMethod()

        view?.showPaymentMethods(paymentMethods)
    }

    private fun validatePaymentMethod() {
        if (selectedPaymentMethod?.method == PaymentMethod.MethodType.BANK_TRANSFER) {
            if (currentAlphaCode == BANK_TRANSFER_UK_CODE) {
                selectCurrency(BuyCurrency.Currency.create(Constants.GBP_SYMBOL))
            } else {
                selectCurrency(BuyCurrency.Currency.create(Constants.EUR_SYMBOL))
            }
        }
    }

    override fun onSelectTokenClicked() {
        buyInteractor.getQuotesByCurrency(selectedCurrency.code).forEach { quote ->
            tokensToBuy.find { it.tokenSymbol == quote.token.tokenSymbol }?.let {
                it.rate = quote.price
                it.currency = quote.currency
            }
        }
        view?.showTokensToBuy(selectedToken, tokensToBuy)
    }

    override fun onSelectCurrencyClicked() {
        if (currencySelectionEnabled) {
            view?.showCurrency(currenciesToSelect, selectedCurrency)
        }
    }

    override fun onTotalClicked() {
        buyAnalytics.logBuyTotalShown()
        buyDetailsState?.let {
            view?.showDetailsBottomSheet(it)
        }
    }

    override fun setTokenToBuy(token: Token) {
        buyAnalytics.logBuyTokenChanged(selectedToken.tokenSymbol, token.tokenSymbol)
        selectedToken = token
        recalculate()
    }

    private fun selectCurrency(currency: BuyCurrency.Currency) {
        view?.setCurrencyCode(currency.code)
        setCurrency(currency, byUser = false)
    }

    override fun setCurrency(currency: BuyCurrency.Currency, byUser: Boolean) {
        if (byUser) {
            buyAnalytics.logBuyCurrencyChanged(selectedCurrency.code, currency.code)
        }
        selectedCurrency = currency

        if (isValidCurrencyForPay()) {
            recalculate()
        }
    }

    private fun isValidCurrencyForPay(): Boolean {
        val selectedCurrencyCode = selectedCurrency.code
        val currentPaymentMethod = selectedPaymentMethod ?: return false

        if (currentPaymentMethod.method == PaymentMethod.MethodType.BANK_TRANSFER) {
            if (selectedCurrencyCode == Constants.USD_READABLE_SYMBOL ||
                (currentAlphaCode == BANK_TRANSFER_UK_CODE && selectedCurrencyCode == Constants.EUR_SYMBOL)
            ) {
                paymentMethods.find { it.method == PaymentMethod.MethodType.CARD }?.let {
                    onPaymentMethodSelected(it, byUser = false)
                }
                return isValidCurrencyForPay()
            } else if (selectedCurrency.code == Constants.GBP_SYMBOL && currentAlphaCode != BANK_TRANSFER_UK_CODE) {
                paymentMethods.find { it.method == PaymentMethod.MethodType.CARD }?.let {
                    onPaymentMethodSelected(it, byUser = false)
                }
                return isValidCurrencyForPay()
            }
        }
        return true
    }

    override fun onFocusFieldChanged(focusField: FocusField) {
        isSwappedToToken = focusField == FocusField.TOKEN
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
            val paymentMethod = selectedPaymentMethod?.paymentType ?: return@launch

            val result = buyInteractor.getMoonpayBuyResult(
                baseCurrencyAmount = amountInCurrency,
                quoteCurrencyAmount = amountInTokens,
                tokenToBuy = selectedToken,
                baseCurrencyCode = baseCurrencyCode,
                paymentMethod = paymentMethod
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
        when (buyResult) {
            is MoonpayBuyResult.Success -> {
                buyResultAnalytics = BuyAnalytics.BuyResult.SUCCESS
                updateViewWithBuyCurrencyData(buyResult.data)
            }
            is MoonpayBuyResult.Error -> {
                Timber.e(buyResult, "Failed to buy")
                buyResultAnalytics = BuyAnalytics.BuyResult.ERROR
                view?.showMessage(buyResult.message)
            }
            is MoonpayBuyResult.MinAmountError -> {
                Timber.i(buyResult.toString(), "Failed to buy: MinAmountError")
                buyResultAnalytics = BuyAnalytics.BuyResult.ERROR
                showMinAmountError(buyResult.minBuyAmount)
            }
            is MoonpayBuyResult.MaxAmountError -> {
                Timber.i(buyResult.toString(), "Failed to buy: MaxAmountError")
                buyResultAnalytics = BuyAnalytics.BuyResult.ERROR
                showMaxAmountError(buyResult.maxBuyAmount)
            }
        }
        buyAnalytics.logBuyPaymentResultShown(buyResultAnalytics)
    }

    private fun showMinAmountError(minAmount: BigDecimal) {
        view?.apply {
            setContinueButtonEnabled(false)
            val symbol = selectedCurrency.code.symbolFromCode()
            val minAmountWithSymbol = "$symbol ${minAmount.formatFiat()}"
            buyDetailsState = BuyDetailsState.MinAmountError(minAmountWithSymbol)
            showMessage(
                resources.getString(R.string.buy_min_transaction_format).format(minAmountWithSymbol)
            )
            clearOppositeFieldAndTotal("${selectedCurrency.code.symbolFromCode()} 0")
        }
    }

    private fun showMaxAmountError(maxAmount: BigDecimal) {
        view?.apply {
            setContinueButtonEnabled(false)
            val symbol = selectedCurrency.code.symbolFromCode()
            val minAmountWithSymbol = "$symbol ${maxAmount.formatFiat()}"
            buyDetailsState = BuyDetailsState.MaxAmountError(minAmountWithSymbol)
            showMessage(
                resources.getString(R.string.buy_max_transaction_format).format(minAmountWithSymbol)
            )
            clearOppositeFieldAndTotal("${selectedCurrency.code.symbolFromCode()} 0")
        }
    }

    private fun onBuyCurrencyLoadFailed(error: Throwable) {
        if (error is CancellationException) {
            Timber.i("Cancelled get currency request")
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
        val amount = if (isSwappedToToken) {
            buyCurrencyInfo.totalAmount.formatFiat()
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
            purchaseCostText = currencyForTokensAmount.asCurrency(currency)
        )
        view?.apply {
            showTotal(data)
            currentBuyViewData = data
            buyDetailsState = BuyDetailsState.Valid(data)
            showMessage(message = null, selectedToken.tokenSymbol)
            setContinueButtonEnabled(true)
        }
    }

    private fun handleEnteredAmountInvalid(loadedBuyCurrency: BuyCurrency.Currency) {
        val isAmountLower = amount.toBigDecimal() < loadedBuyCurrency.minAmount
        val maxAmount = loadedBuyCurrency.maxAmount
        val minAmount = loadedBuyCurrency.minAmount
        val symbol = selectedCurrency.code.symbolFromCode()
        val minAmountMessage = resources.getString(R.string.buy_min_transaction_format)
            .format("$symbol $minAmount")
        val maxAmountMessage = resources.getString(R.string.buy_max_error_format)
            .format("$symbol $maxAmount")
        val message = if (isAmountLower) {
            minAmountMessage
        } else {
            maxAmountMessage
        }
        buyDetailsState = BuyDetailsState.MinAmountError("$symbol ${minAmount.formatFiat()}")
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
            buyAnalytics.logBuyButtonPressed(
                buySumCurrency = it.total.formatFiat(),
                buySumCoin = it.receiveAmount.toBigDecimal().formatFiat(),
                buyCurrency = selectedCurrency.code,
                buyCoin = it.tokenSymbol,
                methodPayment = selectedPaymentMethod
            )
            view?.navigateToMoonpay(
                amount = it.total.toString(),
                selectedToken,
                selectedCurrency,
                paymentType
            )
            buyAnalytics.logBuyMoonPayOpened()
        }
    }

    private fun getValidPaymentType(): String? {
        return if (currentAlphaCode == BANK_TRANSFER_UK_CODE && selectedCurrency.code == Constants.EUR_SYMBOL) {
            SEPA_BANK_TRANSFER
        } else {
            selectedPaymentMethod?.paymentType
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
