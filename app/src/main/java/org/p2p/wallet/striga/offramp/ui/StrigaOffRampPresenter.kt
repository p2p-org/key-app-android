package org.p2p.wallet.striga.offramp.ui

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.core.utils.NoCoverage
import org.p2p.core.utils.formatFiat
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
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState

private const val TAG = "StrigaOffRampPresenter"
private val DEFAULT_INITIAL_AMOUNT = BigDecimal.ZERO

class StrigaOffRampPresenter(
    private val connectionManager: ConnectionManager,
    private val interactor: StrigaOffRampInteractor,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val strigaOffRampMapper: StrigaOffRampMapper,
    private val swapWidgetMapper: StrigaOffRampSwapWidgetMapper,
    private val rateTickerMapper: SwapRateTickerMapper,
) : BasePresenter<View>(), Presenter {

    private val timber: Timber.Tree = Timber.tag(TAG)

    private val viewState = MutableStateFlow(
        StrigaOffRampViewState(
            exchangeRateState = SwapRateTickerState.Loading,
            buttonState = StrigaOffRampButtonState.LoadingRates,
        )
    )

    private var rate: StrigaExchangeRate? = null
    private var balance: BigDecimal = DEFAULT_INITIAL_AMOUNT
    private var inputAmountA: BigDecimal = DEFAULT_INITIAL_AMOUNT
    private var inputAmountB: BigDecimal = DEFAULT_INITIAL_AMOUNT
    private var isErrorHappened: Boolean = false

    private val cannotClickAllAmount: Boolean
        get() = isErrorHappened

    override fun attach(view: View) {
        super.attach(view)
        interactor.startPolling(this)
        observeUsdc()
        observeRateChanges()
        observeInternetState()
        observeViewState()
    }

    override fun detach() {
        super.detach()
        interactor.stopPolling()
    }

    private fun observeUsdc() {
        launch {
            tokenServiceCoordinator.observeUserTokens()
                .collect { handleTokenState(it) }
        }
    }

    private fun observeRateChanges() {
        launch {
            interactor.observeExchangeRateState()
                .collect { state -> handleRateChange(state) }
        }
    }

    private fun observeInternetState() {
        launch {
            connectionManager.connectionStatus.collect { hasConnection ->
                if (hasConnection) {
                    interactor.startPolling(this)

                    // restore after error
                    if (isErrorHappened) {
                        isErrorHappened = false
                        tokenServiceCoordinator.refresh()
                    }
                } else {
                    isErrorHappened = true
                    interactor.stopPolling()

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
        validate()
    }

    override fun onTokenBAmountChange(amountB: String) {
        inputAmountB = amountB.toBigDecimalOrZero()
        setAndCalcTokenAmount(
            targetTokenType = StrigaOffRampTokenType.TokenA,
            amount = inputAmountB
        )
        validate()
    }

    override fun onAllAmountClick() {
        // ignore all amount click when no internet or rate is not loaded/error occurred
        if (!connectionManager.connectionStatus.value || cannotClickAllAmount) return

        setTokenAmount(
            targetTokenType = StrigaOffRampTokenType.TokenA,
            amount = balance
        )
        setAndCalcTokenAmount(
            targetTokenType = StrigaOffRampTokenType.TokenB,
            amount = balance
        )
        validate()
    }

    override fun onSubmit() {
        // todo: https://p2pvalidator.atlassian.net/browse/PWN-9267
    }

    private fun setTokenAmount(targetTokenType: StrigaOffRampTokenType, amount: BigDecimal) {
        when (targetTokenType) {
            StrigaOffRampTokenType.TokenA -> {
                inputAmountA = amount
                val tokenAState = swapWidgetMapper.mapByState(
                    tokenType = StrigaOffRampTokenType.TokenA,
                    state = swapWidgetMapper.mapTokenA(
                        amount = amount,
                        balance = balance,
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

    private fun validate() {
        val buttonState = interactor.validateAmount(
            amountA = inputAmountA,
            amountB = inputAmountB,
            balance = balance
        )
        setButtonState(buttonState)

        if (buttonState.isAmountError) {
            view?.setTokenAErrorState(isError = true)
        } else {
            view?.setTokenAErrorState(isError = false)
        }
    }

    private fun setButtonState(state: StrigaOffRampButtonState) {
        timber.d("Set button state: $state")
        viewState.value = viewState.value.copy(buttonState = state)
    }

    private fun setExchangeRateState(state: SwapRateTickerState) {
        viewState.value = viewState.value.copy(exchangeRateState = state)
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
                    logError(throwable.cause ?: throwable)
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
            is UserTokensState.Idle -> Unit
            is UserTokensState.Loading -> {
                timber.d("Loading USDC balance")
                balance = BigDecimal.ZERO
                setTokenABalance(null)
            }
            is UserTokensState.Refreshing -> Unit
            is UserTokensState.Error -> view?.showErrorMessage(newState.cause)
            is UserTokensState.Empty -> {
                timber.d("USDC balance is empty...")
                balance = BigDecimal.ZERO
                setTokenABalance(balance)
            }
            is UserTokensState.Loaded -> {
                val usdc = newState.solTokens.find { it.isUSDC }
                val balance = usdc?.total ?: BigDecimal.ZERO
                timber.d("USDC balance = ${balance.formatFiat()}")
                this.balance = balance
                setTokenABalance(balance)
            }
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

                validate()
            }
            is StrigaOffRampRateState.Failure -> {
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
                StrigaOffRampTokenState.Loading(balance)
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
                StrigaOffRampTokenState.Disabled(balance)
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
