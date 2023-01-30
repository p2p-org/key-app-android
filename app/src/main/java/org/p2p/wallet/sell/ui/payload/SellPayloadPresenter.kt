package org.p2p.wallet.sell.ui.payload

import android.content.res.Resources
import org.p2p.core.model.CurrencyMode
import org.p2p.core.utils.MOONPAY_DECIMAL
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatTokenForMoonpay
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.uikit.components.SellWidgetViewState
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
import org.p2p.wallet.moonpay.model.MoonpaySellError
import org.p2p.wallet.moonpay.model.MoonpayWidgetUrlBuilder
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.moonpay.repository.sell.MoonpayExternalCustomerIdProvider
import org.p2p.wallet.moonpay.repository.sell.SellTransactionFiatCurrency
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.sell.ui.payload.SellPayloadContract.ViewState
import org.p2p.wallet.utils.emptyString
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

class SellPayloadPresenter(
    private val sellInteractor: SellInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val moonpayWidgetUrlBuilder: MoonpayWidgetUrlBuilder,
    private val externalCustomerIdProvider: MoonpayExternalCustomerIdProvider,
    private val secureStorage: SecureStorageContract,
    private val resources: Resources,
) : BasePresenter<SellPayloadContract.View>(),
    SellPayloadContract.Presenter {

    private var userSolBalance: BigDecimal = BigDecimal.ZERO
    private var minTokenSellAmount: BigDecimal = BigDecimal.ZERO
    private var maxTokenSellAmount: BigDecimal? = null
    private var rawUserSelectedAmount: String = emptyString()
    private val selectedTokenAmount: BigDecimal
        get() = rawToDecimalAmount()
    private var tokenPrice: BigDecimal = BigDecimal.ZERO

    private lateinit var tokenCurrencyMode: CurrencyMode.Token
    private var fiatCurrencyMode: CurrencyMode.Fiat = CurrencyMode.Fiat.Usd
    private lateinit var selectedCurrencyMode: CurrencyMode
    private val currencyModeToSwitch: CurrencyMode
        get() = when (selectedCurrencyMode) {
            is CurrencyMode.Token -> fiatCurrencyMode
            is CurrencyMode.Fiat -> tokenCurrencyMode
        }

    private lateinit var viewState: ViewState

    private var sellQuoteJob: Job? = null
    private var needCheckForSellLock: Boolean = true

    override fun attach(view: SellPayloadContract.View) {
        super.attach(view)
        launch {
            try {
                view.showLoading(isVisible = true)
                needCheckForSellLock = true
                // call order is important!
                checkForSellLock()
                sellInteractor.getTokenForSell().also {
                    tokenCurrencyMode = CurrencyMode.Token(
                        symbol = it.tokenSymbol,
                        fractionLength = MOONPAY_DECIMAL
                    )
                    userSolBalance = it.total
                    view.updateToolbarTitle(it.tokenSymbol)
                }

                // if the screen launched from the fresh - make tokenCurrencyMode default
                if (!this@SellPayloadPresenter::selectedCurrencyMode.isInitialized) {
                    selectedCurrencyMode = tokenCurrencyMode
                }

                loadCurrencies()
                initialLoadSellQuote()
                startLoadSellQuoteJob()
                view.showLoading(isVisible = false)

                if (!secureStorage.getBoolean(Key.KEY_IS_SELL_WARNING_SHOWED, false)) {
                    view.showOnlySolWarning()
                } else {
                    view.showKeyboard()
                }
            } catch (error: Throwable) {
                handleError(error)
            }
        }
    }

    private suspend fun checkForSellLock() {
        if (!needCheckForSellLock) return
        needCheckForSellLock = false
        view?.showButtonLoading(true)
        val userTransactionInProcess = getUserTransactionInProcess()
        view?.showButtonLoading(false)
        if (userTransactionInProcess != null) {
            // make readable in https://p2pvalidator.atlassian.net/browse/PWN-6354
            val amounts = userTransactionInProcess.amounts

            view?.navigateToSellLock(
                SellTransactionViewDetails(
                    transactionId = userTransactionInProcess.transactionId,
                    status = userTransactionInProcess.status,
                    formattedSolAmount = amounts.tokenAmount.formatTokenForMoonpay(),
                    formattedFiatAmount = amounts.amountInFiat.formatFiat(),
                    receiverAddress = userTransactionInProcess.moonpayDepositWalletAddress.base58Value,
                    fiatUiName = userTransactionInProcess.selectedFiat.uiSymbol.uppercase()
                )
            )
        }
    }

    override fun checkSellLock() {
        launch { checkForSellLock() }
    }

    override fun setNeedCheckForSellLock() {
        needCheckForSellLock = true
    }

    private fun rawToDecimalAmount(): BigDecimal =
        when (selectedCurrencyMode) {
            is CurrencyMode.Fiat -> rawUserSelectedAmount.toBigDecimalOrZero().toToken()
            is CurrencyMode.Token -> rawUserSelectedAmount.toBigDecimalOrZero()
        }

    private suspend fun getUserTransactionInProcess(): SellTransaction.WaitingForDepositTransaction? {
        val userTransactions = sellInteractor.loadUserSellTransactions()
        return userTransactions.filterIsInstance<SellTransaction.WaitingForDepositTransaction>()
            .firstOrNull()
    }

    private suspend fun loadCurrencies() {
        val solCurrency = sellInteractor.getSolCurrency()
        minTokenSellAmount = solCurrency.amounts.minSellAmount.orZero()
        maxTokenSellAmount = solCurrency.amounts.maxSellAmount
        rawUserSelectedAmount = minTokenSellAmount.formatTokenForMoonpay()
        fiatCurrencyMode = sellInteractor.getMoonpaySellFiatCurrency().toCurrencyMode()
    }

    private suspend fun initialLoadSellQuote() {
        val sellQuote = sellInteractor.getSellQuoteForSol(
            solAmount = selectedTokenAmount,
            fiat = fiatCurrencyMode.toSellFiatCurrency()
        )
        onSellQuoteLoaded(sellQuote)
    }

    private fun handleError(error: Throwable) {
        when (error) {
            is MoonpaySellError.NoInternetForRequest -> {
                view?.showUiKitSnackBar(messageResId = R.string.common_offline_error)
            }
            is CancellationException -> {
                Timber.i(error)
            }
            else -> {
                Timber.e(error, "Error on loading data from Moonpay $error")
                // navigate to error only if there no old values
                if (tokenPrice.isZero()) {
                    view?.navigateToErrorScreen()
                }
            }
        }
    }

    private fun startLoadSellQuoteJob() {
        sellQuoteJob?.cancel()
        sellQuoteJob = launch {
            while (isActive) {
                try {
                    delay(SELL_QUOTE_REQUEST_DEBOUNCE_TIME)
                    val sellQuote = sellInteractor.getSellQuoteForSol(
                        solAmount = selectedTokenAmount,
                        fiat = fiatCurrencyMode.toSellFiatCurrency()
                    )
                    onSellQuoteLoaded(sellQuote)
                } catch (error: Throwable) {
                    handleError(error)
                }
            }
        }
    }

    private fun onSellQuoteLoaded(sellQuote: MoonpaySellTokenQuote) {
        tokenPrice = sellQuote.tokenPrice

        val widgetViewState = SellWidgetViewState(
            tokenSymbol = tokenCurrencyMode.symbol,
            fiatName = fiatCurrencyMode.fiatAbbreviation,
            currencyMode = selectedCurrencyMode,
            currencyModeToSwitch = currencyModeToSwitch,
            availableTokenAmount = userSolBalance,
            fiatEarningAmount = sellQuote.fiatEarning,
            feeInFiat = sellQuote.feeAmountInFiat,
            feeInToken = sellQuote.feeAmountInToken,
            inputAmount = rawUserSelectedAmount,
            sellQuoteInFiat = sellQuote.tokenPrice
        )
        viewState = ViewState(
            cashOutButtonState = determineButtonState(),
            widgetViewState = widgetViewState,
        )
        view?.updateViewState(viewState)
    }

    override fun switchCurrencyMode() {
        val oldAmount = rawUserSelectedAmount.toBigDecimalOrZero()
        selectedCurrencyMode = currencyModeToSwitch
        val switchedAmount = when (selectedCurrencyMode) {
            is CurrencyMode.Fiat -> oldAmount.toFiat()
            is CurrencyMode.Token -> oldAmount.toToken()
        }
        viewState = viewState.run {
            copy(
                widgetViewState = widgetViewState.copy(
                    currencyMode = selectedCurrencyMode,
                    currencyModeToSwitch = currencyModeToSwitch,
                    inputAmount = switchedAmount.formatTokenForMoonpay()
                )
            )
        }
        view?.updateViewState(viewState)
    }

    override fun cashOut() {
        if (sellInteractor.shouldShowInformDialog()) {
            view?.navigateToInformationScreen()
        } else {
            buildMoonpayWidget()
        }
    }

    override fun buildMoonpayWidget() {
        val userAddress = tokenKeyProvider.publicKey.toBase58Instance()

        val moonpayUrl = moonpayWidgetUrlBuilder.buildSellWidgetUrl(
            tokenSymbol = tokenCurrencyMode.symbol,
            userAddress = userAddress,
            externalCustomerId = externalCustomerIdProvider.getCustomerId(),
            fiatSymbol = fiatCurrencyMode.fiatAbbreviation,
            tokenAmountToSell = selectedTokenAmount.formatTokenForMoonpay(),
        )
        view?.showMoonpayWidget(url = moonpayUrl)
    }

    override fun onTokenAmountChanged(newValue: String) {
        rawUserSelectedAmount = newValue

        val buttonState = determineButtonState()
        when (buttonState) {
            is CashOutButtonState.CashOutAvailable, is CashOutButtonState.NotEnoughUserTokenError -> {
                startLoadSellQuoteJob()
            }
            is CashOutButtonState.MinAmountEntered, is CashOutButtonState.MaxAmountExceeded -> {
                sellQuoteJob?.cancel()
            }
        }
        viewState = viewState.copy(
            cashOutButtonState = buttonState,
            widgetViewState = viewState.widgetViewState.copy(
                inputAmount = rawUserSelectedAmount
            )
        )
        view?.updateViewState(viewState)
    }

    override fun onUserMaxClicked() {
        val maxAmount = when (selectedCurrencyMode) {
            is CurrencyMode.Fiat -> userSolBalance.toFiat()
            is CurrencyMode.Token -> userSolBalance
        }.formatTokenForMoonpay()
        viewState = viewState.copy(
            widgetViewState = viewState.widgetViewState.copy(
                inputAmount = maxAmount
            )
        )
        view?.updateViewState(viewState)
        onTokenAmountChanged(maxAmount)
    }

    private fun determineButtonState(): CashOutButtonState {
        return when {
            selectedTokenAmount.isLessThan(minTokenSellAmount) -> {
                CashOutButtonState.MinAmountEntered(resources, minTokenSellAmount)
            }

            selectedTokenAmount.isMoreThan(maxTokenSellAmount.orZero()) -> {
                CashOutButtonState.MaxAmountExceeded(resources, maxTokenSellAmount.orZero())
            }

            selectedTokenAmount.isMoreThan(userSolBalance) -> {
                CashOutButtonState.NotEnoughUserTokenError(resources)
            }

            else -> {
                CashOutButtonState.CashOutAvailable(resources)
            }
        }
    }

    private fun SellTransactionFiatCurrency.toCurrencyMode(): CurrencyMode.Fiat {
        return when (this) {
            SellTransactionFiatCurrency.USD -> CurrencyMode.Fiat.Usd
            SellTransactionFiatCurrency.EUR -> CurrencyMode.Fiat.Eur
            SellTransactionFiatCurrency.GBP -> CurrencyMode.Fiat.Gbp
        }
    }

    private fun CurrencyMode.Fiat.toSellFiatCurrency(): SellTransactionFiatCurrency {
        return when (this) {
            CurrencyMode.Fiat.Usd -> SellTransactionFiatCurrency.USD
            CurrencyMode.Fiat.Eur -> SellTransactionFiatCurrency.EUR
            CurrencyMode.Fiat.Gbp -> SellTransactionFiatCurrency.GBP
        }
    }

    private fun BigDecimal.toFiat() = this.multiply(tokenPrice).setScale(2, RoundingMode.DOWN)
    private fun BigDecimal.toToken() = this.divide(tokenPrice, 2, RoundingMode.DOWN)
}
