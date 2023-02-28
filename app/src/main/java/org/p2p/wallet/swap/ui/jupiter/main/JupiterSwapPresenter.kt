package org.p2p.wallet.swap.ui.jupiter.main

import java.math.BigDecimal
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.utils.isZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapButtonMapper
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapWidgetMapper
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel

private const val AMOUNT_INPUT_DELAY = 400L

class JupiterSwapPresenter(
    private val managerHolder: SwapStateManagerHolder,
    private val stateManager: SwapStateManager,
    private val widgetMapper: SwapWidgetMapper,
    private val buttonMapper: SwapButtonMapper,
    private val rateLoaderTokenA: SwapTokenRateLoader,
    private val rateLoaderTokenB: SwapTokenRateLoader,
    private val dispatchers: CoroutineDispatchers,
) : BasePresenter<JupiterSwapContract.View>(), JupiterSwapContract.Presenter {

    private var featureState: SwapState? = null
    private var rateTokenAJob: Job? = null
    private var rateTokenBJob: Job? = null
    private var debounceInputJob: Job? = null
    private var widgetAState: SwapWidgetModel = widgetMapper.mapWidgetLoading(tokenType = SwapTokenType.TOKEN_A)
    private var widgetBState: SwapWidgetModel = widgetMapper.mapWidgetLoading(tokenType = SwapTokenType.TOKEN_B)

    override fun attach(view: JupiterSwapContract.View) {
        super.attach(view)

        stateManager.observe()
            .onEach(::handleFeatureState)
            .launchIn(this)
    }

    override fun switchTokens() {
        stateManager.onNewAction(SwapStateAction.SwitchTokens)
    }

    override fun onTokenAmountChange(amount: String) {
        debounceInputJob?.cancel()
        cancelRateJobs()
        debounceInputJob = launch {
            val newAmount = amount.toBigDecimalOrZero()
            val action = if (newAmount.isZero()) {
                SwapStateAction.EmptyAmountTokenA
            } else {
                val pair = featureState?.getTokensPair()
                val tokenA = pair?.first
                val tokenB = pair?.second
                if (tokenB != null) {
                    view?.setSecondTokenWidgetState(widgetMapper.mapTokenBLoading(token = tokenB))
                }
                if (tokenA != null) {
                    getRateTokenA(tokenA, newAmount)
                }
                stateManager.onNewAction(SwapStateAction.CancelSwapLoading)
                delay(AMOUNT_INPUT_DELAY)
                SwapStateAction.TokenAAmountChanged(newAmount)
            }
            stateManager.onNewAction(action)
        }
    }

    private fun cancelRateJobs() {
        rateTokenAJob?.cancel()
        rateTokenBJob?.cancel()
    }

    private fun SwapState.getTokensPair(): Pair<SwapTokenModel?, SwapTokenModel?> {
        return when (this) {
            SwapState.InitialLoading -> null to null
            is SwapState.LoadingRoutes -> tokenA to tokenB
            is SwapState.LoadingTransaction -> tokenA to tokenB
            is SwapState.SwapException -> previousFeatureState.getTokensPair()
            is SwapState.SwapLoaded -> tokenA to tokenB
            is SwapState.TokenAZero -> tokenA to tokenB
        }
    }

    override fun onSwapTokenClick() {
        stateManager.onNewAction(SwapStateAction.SwapSuccess)
    }

    override fun onAllAmountClick() {
        val allTokenAAmount = when (val featureState = featureState) {
            SwapState.InitialLoading,
            is SwapState.SwapLoaded,
            is SwapState.TokenAZero,
            is SwapState.LoadingRoutes,
            is SwapState.LoadingTransaction -> getTokenAAmount(featureState)
            is SwapState.SwapException -> getTokenAAmount(featureState.previousFeatureState)
            null -> null
        }
        if (allTokenAAmount != null) {
            cancelRateJobs()
            stateManager.onNewAction(SwapStateAction.TokenAAmountChanged(allTokenAAmount))
        }
    }

    private fun getTokenAAmount(state: SwapState): BigDecimal? {
        val tokenA = when (state) {
            is SwapState.LoadingRoutes -> state.tokenA
            is SwapState.LoadingTransaction -> state.tokenA
            is SwapState.SwapLoaded -> state.tokenA
            is SwapState.TokenAZero -> state.tokenA
            SwapState.InitialLoading,
            is SwapState.SwapException.FeatureExceptionWrapper,
            is SwapState.SwapException.OtherException -> null
        }
        return (tokenA as? SwapTokenModel.UserToken)?.details?.total
    }

    override fun onChangeTokenAClick() {
        if (isChangeTokenScreenAvailable(featureState)) {
            view?.openChangeTokenAScreen()
        }
    }

    override fun onChangeTokenBClick() {
        if (isChangeTokenScreenAvailable(featureState)) {
            view?.openChangeTokenBScreen()
        }
    }

    private fun isChangeTokenScreenAvailable(featureState: SwapState?): Boolean {
        return when (featureState) {
            null,
            SwapState.InitialLoading -> false
            is SwapState.LoadingRoutes,
            is SwapState.LoadingTransaction,
            is SwapState.SwapLoaded,
            is SwapState.TokenAZero -> true
            is SwapState.SwapException ->
                isChangeTokenScreenAvailable(featureState.previousFeatureState)
        }
    }

    override fun onBackPressed() {
        view?.closeScreen()
    }

    override fun finishFeature(stateManagerHolderKey: String) {
        managerHolder.clear(stateManagerHolderKey)
    }

    private fun handleFeatureState(state: SwapState) {
        cancelRateJobs()
        when (state) {
            SwapState.InitialLoading -> handleInitialLoading()
            is SwapState.TokenAZero -> handleTokenAZero(state)
            is SwapState.LoadingRoutes -> handleLoadingRoutes(state)
            is SwapState.LoadingTransaction -> handleLoadingTransaction(state)
            is SwapState.SwapLoaded -> handleSwapLoaded(state)
            is SwapState.SwapException.FeatureExceptionWrapper -> {
                // todo
            }
            is SwapState.SwapException.OtherException -> {
                // todo
            }
        }
        featureState = state
    }

    private fun handleSwapLoaded(state: SwapState.SwapLoaded) {
        widgetAState = widgetMapper.mapTokenAAndSaveOldFiatAmount(
            oldWidgetModel = widgetAState,
            token = state.tokenA,
            tokenAmount = state.amountTokenA
        )
        widgetBState = widgetMapper.mapTokenBAndSaveOldFiatAmount(
            oldWidgetModel = widgetBState,
            token = state.tokenB,
            tokenAmount = state.amountTokenB,
        )
        updateWidgets()
        view?.setButtonState(
            buttonState = buttonMapper.mapReadyToSwap(tokenA = state.tokenA, tokenB = state.tokenB)
        )
        getRateTokenA(tokenA = state.tokenA, tokenAmount = state.amountTokenA)
        getRateTokenB(tokenB = state.tokenB, tokenAmount = state.amountTokenB)
    }

    private fun handleLoadingTransaction(state: SwapState.LoadingTransaction) {
        widgetAState = widgetMapper.mapTokenAAndSaveOldFiatAmount(
            oldWidgetModel = widgetAState,
            token = state.tokenA,
            tokenAmount = state.amountTokenA
        )
        widgetBState = widgetMapper.mapTokenBAndSaveOldFiatAmount(
            oldWidgetModel = widgetBState,
            token = state.tokenB,
            tokenAmount = state.amountTokenB,
        )
        updateWidgets()
        view?.setButtonState(buttonState = buttonMapper.mapLoading())
        getRateTokenA(tokenA = state.tokenA, tokenAmount = state.amountTokenA)
        getRateTokenB(tokenB = state.tokenB, tokenAmount = state.amountTokenB)
    }

    private fun handleLoadingRoutes(state: SwapState.LoadingRoutes) {
        widgetAState = widgetMapper.mapTokenAAndSaveOldFiatAmount(
            oldWidgetModel = widgetAState,
            token = state.tokenA,
            tokenAmount = state.amountTokenA
        )
        widgetBState = widgetMapper.mapTokenBLoading(token = state.tokenB)
        updateWidgets()
        view?.setButtonState(buttonState = buttonMapper.mapLoading())
        getRateTokenA(tokenA = state.tokenA, tokenAmount = state.amountTokenA)
    }

    private fun handleTokenAZero(state: SwapState.TokenAZero) {
        widgetAState = widgetMapper.mapTokenA(token = state.tokenA, tokenAmount = null)
        widgetBState = widgetMapper.mapTokenB(token = state.tokenB, tokenAmount = null)
        updateWidgets()
        view?.setButtonState(buttonMapper.mapEnterAmount())
    }

    private fun handleInitialLoading() {
        widgetAState = widgetMapper.mapWidgetLoading(tokenType = SwapTokenType.TOKEN_A)
        widgetBState = widgetMapper.mapWidgetLoading(tokenType = SwapTokenType.TOKEN_B)
        updateWidgets()
        view?.setButtonState(buttonState = SwapButtonState.Hide)
    }

    private fun getRateTokenA(tokenA: SwapTokenModel, tokenAmount: BigDecimal) {
        rateTokenAJob?.cancel()
        rateTokenAJob = rateLoaderTokenA.getRate(tokenA)
            .flowOn(dispatchers.io)
            .onEach { handleRateLoader(state = it, tokenType = SwapTokenType.TOKEN_A, tokenAmount = tokenAmount) }
            .launchIn(this)
    }

    private fun getRateTokenB(tokenB: SwapTokenModel, tokenAmount: BigDecimal) {
        rateTokenBJob?.cancel()
        rateTokenBJob = rateLoaderTokenB.getRate(tokenB)
            .flowOn(dispatchers.io)
            .onEach { handleRateLoader(state = it, tokenType = SwapTokenType.TOKEN_B, tokenAmount = tokenAmount) }
            .launchIn(this)
    }

    private fun handleRateLoader(
        state: SwapRateLoaderState,
        tokenType: SwapTokenType,
        tokenAmount: BigDecimal,
    ) {
        val widgetModel = when (tokenType) {
            SwapTokenType.TOKEN_A -> widgetAState
            SwapTokenType.TOKEN_B -> widgetBState
        } as? SwapWidgetModel.Content ?: return
        val newWidgetModel = widgetMapper.mapFiatAmount(
            state = state,
            widgetModel = widgetModel,
            tokenAmount = tokenAmount
        )
        when (tokenType) {
            SwapTokenType.TOKEN_A -> {
                widgetAState = newWidgetModel
                view?.setFirstTokenWidgetState(state = widgetAState)
            }
            SwapTokenType.TOKEN_B -> {
                // todo price impact
                /*var fiatAmount = fiatAmount(token, tokenAmount)
                if (true) {
                    fiatAmount = fiatAmount?.copy(textColor = R.color.text_night)
                }*/
                widgetBState = newWidgetModel
                view?.setSecondTokenWidgetState(state = widgetBState)
            }
        }
    }

    private fun updateWidgets() {
        view?.setFirstTokenWidgetState(state = widgetAState)
        view?.setSecondTokenWidgetState(state = widgetBState)
    }
}
