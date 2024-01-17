package org.p2p.wallet.moonpay.ui.new

import android.content.res.Resources
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.analytics.constants.ScreenNames
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.Constants.EUR_READABLE_SYMBOL
import org.p2p.core.utils.asCurrency
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleShort
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.uikit.components.FocusField
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.ui.select.bottomsheet.SelectCurrencyBottomSheet
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.moonpay.interactor.BANK_TRANSFER_UK_CODE
import org.p2p.wallet.moonpay.interactor.BuyInteractor
import org.p2p.wallet.moonpay.interactor.BuyInteractor.Companion.HARDCODED_MIN_BUY_CURRENCY_AMOUNT
import org.p2p.wallet.moonpay.interactor.PaymentMethodsInteractor
import org.p2p.wallet.moonpay.interactor.SEPA_BANK_TRANSFER
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.BuyDetailsState
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.repository.sell.FiatCurrency
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.emptyString

private const val DELAY_IN_MS = 500L

class NewBuyPresenter(
    tokenToBuy: Token,
    private val fiatToken: String? = null,
    private val fiatAmount: String? = null,
    private val preselectedMethodType: PaymentMethod.MethodType? = null,
    private val buyAnalytics: BuyAnalytics,
    private val userInteractor: UserInteractor,
    private val paymentMethodsInteractor: PaymentMethodsInteractor,
    private val resources: Resources,
    private val buyInteractor: BuyInteractor,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
) : BasePresenter<NewBuyContract.View>(), NewBuyContract.Presenter {

    private lateinit var tokensToBuy: List<Token>

    private val paymentMethods = mutableListOf<PaymentMethod>()

    private var selectedCurrency: FiatCurrency = SelectCurrencyBottomSheet.DEFAULT_CURRENCY
    private var selectedToken: Token = tokenToBuy
    private var selectedPaymentMethod: PaymentMethod? = null
    private var currentAlpha3Code: String = emptyString()

    private var amount: String = "0"
    private var isSwappedToToken: Boolean = false
    private var buyDetailsState: BuyDetailsState? = null
    private var currentBuyViewData: BuyViewData? = null

    private var calculationJob: Job? = null

    private val currenciesToSelect = listOf(
        FiatCurrency.GBP,
        FiatCurrency.EUR,
        FiatCurrency.USD
    )

    override fun attach(view: NewBuyContract.View) {
        super.attach(view)
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
            currentAlpha3Code = paymentMethodsInteractor.getBankTransferAlphaCode()

            val availablePaymentMethods = paymentMethodsInteractor.getAvailablePaymentMethods(
                currentAlpha3Code, preselectedMethodType
            )
            selectedPaymentMethod = availablePaymentMethods.first { it.isSelected }
            paymentMethods.addAll(availablePaymentMethods)

            if (paymentMethods.size > 1) {
                view?.showPaymentMethods(paymentMethods)
            } else {
                view?.showPaymentMethods(null)
            }

            validatePaymentMethod()

            if (!fiatToken.isNullOrBlank()) {
                currenciesToSelect.firstOrNull { it.abbriviation.lowercase() == fiatToken.lowercase() }
                    ?.also { selectCurrency(it) }
            }

            if (fiatAmount != null && fiatAmount.toBigDecimalOrNull() != null) {
                selectMinimalFiatAmount(fiatAmount)
            } else {
                preselectMinimalFiatAmount()
            }
        }
    }

    private suspend fun loadMoonpayBuyQuotes() {
        buyInteractor.loadQuotes(currenciesToSelect, tokensToBuy)
    }

    private fun selectMinimalFiatAmount(amount: String) {
        view?.showPreselectedAmount(amount)
        setBuyAmount(amount, isDelayEnabled = false)
    }

    private fun preselectMinimalFiatAmount() {
        selectMinimalFiatAmount(HARDCODED_MIN_BUY_CURRENCY_AMOUNT.toString())
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
            if (currentAlpha3Code == BANK_TRANSFER_UK_CODE) {
                selectCurrency(FiatCurrency.GBP)
            } else {
                selectCurrency(FiatCurrency.EUR)
            }
        }
    }

    override fun onSelectTokenClicked() {
        buyInteractor.getQuotesByCurrency(selectedCurrency).forEach { quote ->
            tokensToBuy.find { it.tokenSymbol == quote.token.tokenSymbol }?.let {
                it.rate = quote.price
                it.currency = quote.currency.abbriviation.uppercase()
            }
        }
        view?.showTokensToBuy(selectedToken, tokensToBuy)
    }

    override fun onSelectCurrencyClicked() {
        view?.showCurrency(currenciesToSelect, selectedCurrency)
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

    private fun selectCurrency(currency: FiatCurrency) {
        view?.setCurrencyCode(currency.abbriviation)
        setCurrency(currency, byUser = false)
    }

    override fun setCurrency(currency: FiatCurrency, byUser: Boolean) {
        if (byUser) {
            buyAnalytics.logBuyCurrencyChanged(selectedCurrency.abbriviation, currency.abbriviation)
        }
        selectedCurrency = currency

        if (isValidCurrencyForPay()) {
            recalculate()
        }
    }

    private fun selectPaymentMethod(methodType: PaymentMethod.MethodType) {
        paymentMethods.find { it.method == methodType }?.let {
            onPaymentMethodSelected(it, byUser = false)
        }
        if (isValidCurrencyForPay()) {
            recalculate()
        }
    }

    private fun isValidCurrencyForPay(): Boolean {
        val selectedCurrencyCode = selectedCurrency.abbriviation
        val currentPaymentMethod = selectedPaymentMethod ?: return false

        if (currentPaymentMethod.method == PaymentMethod.MethodType.BANK_TRANSFER) {
            if (selectedCurrencyCode == Constants.USD_READABLE_SYMBOL ||
                (currentAlpha3Code == BANK_TRANSFER_UK_CODE && selectedCurrencyCode == EUR_READABLE_SYMBOL)
            ) {
                paymentMethods.find { it.method == PaymentMethod.MethodType.CARD }?.let {
                    onPaymentMethodSelected(it, byUser = false)
                }
                return isValidCurrencyForPay()
            } else if (selectedCurrency == FiatCurrency.GBP && currentAlpha3Code != BANK_TRANSFER_UK_CODE) {
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

            val paymentMethod = selectedPaymentMethod?.paymentType ?: return@launch

            val result = buyInteractor.getMoonpayBuyResult(
                baseCurrencyAmount = amountInCurrency,
                quoteCurrencyAmount = amountInTokens,
                tokenToBuy = selectedToken,
                baseCurrencyCode = selectedCurrency,
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
            val symbol = selectedCurrency.uiSymbol
            val minAmountWithSymbol = "$symbol ${minAmount.formatFiat()}"
            buyDetailsState = BuyDetailsState.MinAmountError(minAmountWithSymbol)
            showMessage(
                resources.getString(R.string.buy_min_transaction_format).format(minAmountWithSymbol)
            )
            clearOppositeFieldAndTotal("${selectedCurrency.uiSymbol} 0")
        }
    }

    private fun showMaxAmountError(maxAmount: BigDecimal) {
        view?.apply {
            setContinueButtonEnabled(false)
            val symbol = selectedCurrency.uiSymbol
            val minAmountWithSymbol = "$symbol ${maxAmount.formatFiat()}"
            buyDetailsState = BuyDetailsState.MaxAmountError(minAmountWithSymbol)
            showMessage(
                resources.getString(R.string.buy_max_transaction_format).format(minAmountWithSymbol)
            )
            clearOppositeFieldAndTotal("${selectedCurrency.uiSymbol} 0")
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

        val enteredAmountHigherThanMin =
            buyCurrencyInfo.totalFiatAmount >= HARDCODED_MIN_BUY_CURRENCY_AMOUNT.toBigDecimal()

        if (enteredAmountHigherThanMin && enteredAmountLowerThanMax) {
            handleEnteredAmountValid(buyCurrencyInfo)
        } else {
            handleEnteredAmountInvalid(loadedBuyCurrency)
        }
    }

    private fun handleEnteredAmountValid(buyCurrencyInfo: BuyCurrency) {
        val amount = if (isSwappedToToken) {
            buyCurrencyInfo.totalFiatAmount.formatFiat()
        } else {
            buyCurrencyInfo.receiveAmount.toBigDecimal().formatToken()
        }
        val currencyForTokensAmount = buyCurrencyInfo.price * buyCurrencyInfo.receiveAmount.toBigDecimal()
        val data = BuyViewData(
            tokenSymbol = selectedToken.tokenSymbol,
            currencySymbol = selectedCurrency.uiSymbol,
            price = buyCurrencyInfo.price.scaleShort(),
            receiveAmount = buyCurrencyInfo.receiveAmount,
            processingFee = buyCurrencyInfo.feeAmount.scaleShort(),
            networkFee = buyCurrencyInfo.networkFeeAmount.scaleShort(),
            extraFee = buyCurrencyInfo.extraFeeAmount.scaleShort(),
            accountCreationCost = null,
            total = buyCurrencyInfo.totalFiatAmount.scaleShort(),
            receiveAmountText = amount,
            purchaseCostText = currencyForTokensAmount.asCurrency(selectedCurrency.uiSymbol)
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
        val minAmount = HARDCODED_MIN_BUY_CURRENCY_AMOUNT.toBigDecimal()
        val maxAmount = loadedBuyCurrency.maxAmount
        val isAmountLower = amount.toBigDecimal() < minAmount
        val symbol = selectedCurrency.uiSymbol
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
            clearOppositeFieldAndTotal("${selectedCurrency.uiSymbol} 0")
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
                buyCurrency = selectedCurrency.uiSymbol,
                buyCoin = it.tokenSymbol,
                methodPayment = selectedPaymentMethod
            )
            view?.navigateToMoonpay(
                amount = it.total.toString(),
                selectedToken = selectedToken,
                selectedCurrency = selectedCurrency,
                paymentMethod = paymentType
            )
            buyAnalytics.logBuyMoonPayOpened()
        }
    }

    private fun getValidPaymentType(): String? {
        return if (currentAlpha3Code == BANK_TRANSFER_UK_CODE && selectedCurrency == FiatCurrency.EUR) {
            SEPA_BANK_TRANSFER
        } else {
            selectedPaymentMethod?.paymentType
        }
    }

    override fun detach() {
        calculationJob?.cancel()
        super.detach()
    }
}
