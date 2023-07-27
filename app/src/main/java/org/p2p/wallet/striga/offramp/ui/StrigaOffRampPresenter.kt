package org.p2p.wallet.striga.offramp.ui

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.ConnectionManager
import org.p2p.core.utils.NoCoverage
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.jupiter.model.SwapRateTickerState
import org.p2p.wallet.jupiter.ui.main.mapper.SwapRateTickerMapper
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.exchange.models.StrigaExchangeRate
import org.p2p.wallet.striga.offramp.StrigaOffRampContract.Presenter
import org.p2p.wallet.striga.offramp.StrigaOffRampContract.View
import org.p2p.wallet.striga.offramp.interactor.StrigaOffRampInteractor
import org.p2p.wallet.striga.offramp.mappers.StrigaOffRampMapper
import org.p2p.wallet.striga.offramp.mappers.StrigaOffRampSwapWidgetMapper
import org.p2p.wallet.striga.offramp.models.StrigaOffRampButtonState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampRateState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampTokenState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampTokenType
import org.p2p.wallet.striga.user.interactor.StrigaSignupDataEnsurerInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState

private const val TAG = "StrigaOffRampPresenter"
private val DEFAULT_INITIAL_AMOUNT = BigDecimal.ZERO

class StrigaOffRampPresenter(
    dispatchers: CoroutineDispatchers,
    private val connectionManager: ConnectionManager,
    private val interactor: StrigaOffRampInteractor,
    private val strigaSignupDataEnsurerInteractor: StrigaSignupDataEnsurerInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val strigaOffRampMapper: StrigaOffRampMapper,
    private val swapWidgetMapper: StrigaOffRampSwapWidgetMapper,
    private val rateTickerMapper: SwapRateTickerMapper,
) : BasePresenter<View>(dispatchers.ui), Presenter {

    private val timber: Timber.Tree = Timber.tag(TAG)

    private val viewState = MutableStateFlow(
        StrigaOffRampViewState(
            exchangeRateState = SwapRateTickerState.Loading,
            buttonState = StrigaOffRampButtonState.LoadingRates,
        )
    )

    private var rate: StrigaExchangeRate? = null
    private var usdcBalance: BigDecimal = DEFAULT_INITIAL_AMOUNT
    private var inputAmountA: BigDecimal = DEFAULT_INITIAL_AMOUNT
    private var inputAmountB: BigDecimal = DEFAULT_INITIAL_AMOUNT
    private var isErrorHappened: Boolean = false

    private val cannotClickAllAmount: Boolean
        get() = isErrorHappened

    override fun attach(view: View) {
        super.attach(view)
        launch {
            runCatching { strigaSignupDataEnsurerInteractor.ensureNeededDataLoaded() }
                .onFailure { Timber.e(it, "Unable to load Striga signup data") }
        }
        interactor.startExchangeRateNotifier(this)
        observeUsdc()
        observeRateChanges()
        observeInternetState()
        observeViewState()
    }

    override fun detach() {
        super.detach()
        interactor.stopExchangeRateNotifier()
    }

    private fun observeUsdc() {
        launch {
            tokenServiceCoordinator.observeUserTokens()
                .collect(::handleTokenState)
        }
    }

    private fun observeRateChanges() {
        launch {
            interactor.observeExchangeRateState()
                .collect(::handleRateChange)
        }
    }

    private fun observeInternetState() {
        launch {
            connectionManager.connectionStatus.collect { hasConnection ->
                if (hasConnection) {
                    interactor.startExchangeRateNotifier(this)

                    // restore after error
                    if (isErrorHappened) {
                        isErrorHappened = false
                        tokenServiceCoordinator.refresh()
                    }
                } else {
                    isErrorHappened = true
                    interactor.stopExchangeRateNotifier()

                    setExchangeRateState(strigaOffRampMapper.mapRateError())
                    setTokensAreDisabled()
                    setButtonState(StrigaOffRampButtonState.ErrorGeneral)

                    view?.showUiKitSnackBar(messageResId = R.string.error_no_internet_message)
                }
            }
        }
    }

    private fun observeViewState() {
        launch {
            viewState.collect {
                view?.setRatioState(rateTickerMapper.mapRate(it.exchangeRateState))
                view?.setButtonState(it.buttonState)
            }
        }
    }

    override fun onTokenAAmountChange(amountA: String) {
        inputAmountA = amountA.toBigDecimalOrZero()
        setAndCalcTokenAmount(
            targetTokenType = StrigaOffRampTokenType.TokenB,
            amount = inputAmountA
        )
        validateView()
    }

    override fun onTokenBAmountChange(amountB: String) {
        inputAmountB = amountB.toBigDecimalOrZero()
        setAndCalcTokenAmount(
            targetTokenType = StrigaOffRampTokenType.TokenA,
            amount = inputAmountB
        )
        validateView()
    }

    override fun onAllAmountClick() {
        // ignore all amount click when no internet or rate is not loaded/error occurred
        if (!connectionManager.connectionStatus.value || cannotClickAllAmount) return

        setTokenAmount(
            targetTokenType = StrigaOffRampTokenType.TokenA,
            amount = usdcBalance
        )
        setAndCalcTokenAmount(
            targetTokenType = StrigaOffRampTokenType.TokenB,
            amount = usdcBalance
        )
        validateView()
    }

    override fun onSubmit() {
        setButtonState(StrigaOffRampButtonState.NextProgress)

        if (strigaUserInteractor.isKycApproved) {
            navigateToWithdrawFlow()
        } else {
            navigateToSignupFlow()
        }
    }

    private fun navigateToWithdrawFlow() {
        launch {
            try {
                // load all necessary data again if it was not loaded before
                // enrich crypto + enrich EUR
                strigaWalletInteractor.loadDetailsForStrigaAccounts().getOrThrow()
                // load statement to extract iban & bic
                strigaWalletInteractor.getEurBankingDetails()

                // go to withdraw screen
                view?.navigateToWithdraw(inputAmountA)
            } catch (e: Throwable) {
                Timber.e(e, "Unable to start Striga withdrawal process")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            } finally {
                setButtonState(StrigaOffRampButtonState.Enabled)
            }
        }
    }

    private fun navigateToSignupFlow() {
        // go to standard Striga signup flow
        val destination = strigaUserInteractor.getUserDestination()
        if (destination == StrigaUserStatusDestination.KYC_PENDING) {
            view?.showKycPendingDialog()
        } else {
            view?.navigateToSignup(destination)
        }
    }

    private fun setTokenAmount(targetTokenType: StrigaOffRampTokenType, amount: BigDecimal) {
        when (targetTokenType) {
            StrigaOffRampTokenType.TokenA -> {
                inputAmountA = amount
                val tokenAState = swapWidgetMapper.mapByState(
                    tokenType = StrigaOffRampTokenType.TokenA,
                    state = swapWidgetMapper.mapTokenA(
                        amount = amount,
                        balance = usdcBalance,
                    )
                )
                view?.setTokenAWidgetState(tokenAState)
            }
            StrigaOffRampTokenType.TokenB -> {
                inputAmountB = amount
                val tokenBState = swapWidgetMapper.mapTokenB(amount)
                view?.setTokenBWidgetState(
                    swapWidgetMapper.mapByState(
                        tokenType = StrigaOffRampTokenType.TokenB,
                        state = tokenBState
                    )
                )
            }
        }
    }

    private fun setAndCalcTokenAmount(targetTokenType: StrigaOffRampTokenType, amount: BigDecimal) {
        val calculatedAmount = interactor.calculateAmountByRate(targetTokenType, rate, amount)
        setTokenAmount(targetTokenType, calculatedAmount)
    }

    private fun validateView() {
        val buttonState = strigaOffRampMapper.mapButtonStateByAmountsAndTotalBalance(
            amountA = inputAmountA,
            amountB = inputAmountB,
            totalBalance = usdcBalance
        )
        setButtonState(buttonState)

        view?.setTokenATextColorRes(
            if (buttonState.isAmountError) {
                R.color.text_rose
            } else {
                R.color.text_night
            }
        )
    }

    private fun setButtonState(state: StrigaOffRampButtonState) {
        timber.d("Set button state: $state")
        launch {
            viewState.emit(viewState.value.copy(buttonState = state))
        }
    }

    private fun setExchangeRateState(state: SwapRateTickerState) {
        timber.d("Set ratio state: $state")
        launch {
            viewState.emit(viewState.value.copy(exchangeRateState = state))
        }
    }

    @NoCoverage
    private fun logError(throwable: Throwable) {
        when (throwable) {
            is CancellationException -> {
                if (!connectionManager.connectionStatus.value) {
                    timber.i(throwable, "Canceled rate polling due to no internet connection")
                } else {
                    timber.i(
                        throwable,
                        "Canceled rate polling due to coroutine has been disposed"
                    )
                }
            }
            is StrigaDataLayerError.InternalError -> {
                if (throwable.cause != null) {
                    logError(throwable.cause!!)
                } else {
                    timber.e(throwable, "DataLayer: Error while rate polling")
                }
            }
            else -> {
                timber.e(throwable, "Error while rate polling")
            }
        }
    }

    private fun handleTokenState(newState: UserTokensState) {
        when (newState) {
            is UserTokensState.Loading -> {
                timber.d("Loading USDC balance")
                usdcBalance = BigDecimal.ZERO
                setTokenABalance(null)
            }
            is UserTokensState.Empty -> {
                timber.d("USDC balance is empty")
                usdcBalance = BigDecimal.ZERO
                setTokenABalance(usdcBalance)
            }
            is UserTokensState.Loaded -> {
                usdcBalance = newState.solTokens.find { it.isUSDC }?.total.orZero()
                timber.d("USDC balance = ${usdcBalance.formatFiat()}")
                setTokenABalance(usdcBalance)
            }
            is UserTokensState.Error -> Unit
            else -> Unit
        }
    }

    private fun handleRateChange(state: StrigaOffRampRateState) {
        when (state) {
            is StrigaOffRampRateState.Loading -> {
                setExchangeRateState(strigaOffRampMapper.mapRateLoading())
                setTokensAreLoading()
                setButtonState(StrigaOffRampButtonState.LoadingRates)
            }
            is StrigaOffRampRateState.Success -> {
                rate = state.rate

                setExchangeRateState(strigaOffRampMapper.mapRateShown(state.rate))
                setTokenAmount(StrigaOffRampTokenType.TokenA, inputAmountA)
                setAndCalcTokenAmount(
                    targetTokenType = StrigaOffRampTokenType.TokenB,
                    amount = inputAmountA
                )

                validateView()
            }
            is StrigaOffRampRateState.Failure -> {
                interactor.stopExchangeRateNotifier()
                isErrorHappened = true
                logError(state.throwable)

                setExchangeRateState(strigaOffRampMapper.mapRateError())
                setTokensAreDisabled()
                setButtonState(StrigaOffRampButtonState.ErrorGeneral)

                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private fun setTokensAreLoading() {
        view?.setTokenAWidgetState(
            swapWidgetMapper.mapByState(
                StrigaOffRampTokenType.TokenA,
                StrigaOffRampTokenState.Loading(usdcBalance)
            )
        )
        view?.setTokenBWidgetState(
            swapWidgetMapper.mapByState(
                StrigaOffRampTokenType.TokenB,
                StrigaOffRampTokenState.Loading()
            )
        )
    }

    private fun setTokensAreDisabled() {
        view?.setTokenAWidgetState(
            swapWidgetMapper.mapByState(
                StrigaOffRampTokenType.TokenA,
                StrigaOffRampTokenState.Disabled(usdcBalance)
            )
        )
        view?.setTokenBWidgetState(
            swapWidgetMapper.mapByState(
                StrigaOffRampTokenType.TokenB,
                StrigaOffRampTokenState.Disabled()
            )
        )
    }

    private fun setTokenABalance(balance: BigDecimal? = null) {
        val tokenAState = if (balance == null) {
            swapWidgetMapper.mapByState(
                tokenType = StrigaOffRampTokenType.TokenA,
                state = swapWidgetMapper.mapTokenALoadingBalance(inputAmountA)
            )
        } else {
            swapWidgetMapper.mapByState(
                tokenType = StrigaOffRampTokenType.TokenA,
                state = swapWidgetMapper.mapTokenA(
                    amount = inputAmountA,
                    balance = balance,
                )
            )
        }
        view?.setTokenAWidgetState(tokenAState)
    }
}
