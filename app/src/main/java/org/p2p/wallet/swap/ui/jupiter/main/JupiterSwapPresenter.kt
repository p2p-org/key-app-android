package org.p2p.wallet.swap.ui.jupiter.main

import java.math.BigDecimal
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.p2p.core.utils.isZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapButtonMapper
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapWidgetMapper
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel

class JupiterSwapPresenter(
    private val managerHolder: SwapStateManagerHolder,
    private val stateManager: SwapStateManager,
    private val widgetMapper: SwapWidgetMapper,
    private val buttonMapper: SwapButtonMapper,
    private val rateLoaderTokenA: SwapTokenRateLoader,
    private val rateLoaderTokenB: SwapTokenRateLoader,
    private val dispatchers: CoroutineDispatchers,
) : BasePresenter<JupiterSwapContract.View>(), JupiterSwapContract.Presenter {

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
        val newAmount = amount.toBigDecimalOrZero()
        val action = if (newAmount.isZero()) {
            SwapStateAction.EmptyAmountTokenA
        } else {
            SwapStateAction.TokenAAmountChanged(newAmount)
        }
        stateManager.onNewAction(action)
    }

    override fun onSwapTokenClick() {
        stateManager.onNewAction(SwapStateAction.SwapSuccess)
    }

    override fun finishFeature(stateManagerHolderKey: String) {
        managerHolder.clear(stateManagerHolderKey)
    }

    private fun handleFeatureState(state: SwapState) {
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
    }

    private fun handleSwapLoaded(state: SwapState.SwapLoaded) {
        widgetAState = widgetMapper.mapTokenA(token = state.tokenA, tokenAmount = state.amountTokenA)
        widgetBState = widgetMapper.mapTokenB(token = state.tokenB, tokenAmount = state.amountTokenB)
        updateWidgets()
        view?.setButtonState(
            buttonState = buttonMapper.mapReadyToSwap(tokenA = state.tokenA, tokenB = state.tokenB)
        )
        getRateTokenA(tokenA = state.tokenA, tokenAmount = state.amountTokenA)
        getRateTokenB(tokenB = state.tokenB, tokenAmount = state.amountTokenB)
    }

    private fun handleLoadingTransaction(state: SwapState.LoadingTransaction) {
        widgetAState = widgetMapper.mapTokenA(token = state.tokenA, tokenAmount = state.amountTokenA)
        widgetBState = widgetMapper.mapTokenB(token = state.tokenB, tokenAmount = state.amountTokenB)
        updateWidgets()
        view?.setButtonState(buttonState = buttonMapper.mapLoading())
        getRateTokenA(tokenA = state.tokenA, tokenAmount = state.amountTokenA)
        getRateTokenB(tokenB = state.tokenB, tokenAmount = state.amountTokenB)
    }

    private fun handleLoadingRoutes(state: SwapState.LoadingRoutes) {
        widgetAState = widgetMapper.mapTokenA(token = state.tokenA, tokenAmount = state.amountTokenA)
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
            SwapTokenType.TOKEN_A -> widgetAState = newWidgetModel
            SwapTokenType.TOKEN_B -> {
                // todo price impact
                /*var fiatAmount = fiatAmount(token, tokenAmount)
                if (true) {
                    fiatAmount = fiatAmount?.copy(textColor = R.color.text_night)
                }*/
                widgetBState = newWidgetModel
            }
        }
        updateWidgets()
    }

    private fun updateWidgets() {
        view?.setFirstTokenWidgetState(state = widgetAState)
        view?.setSecondTokenWidgetState(state = widgetBState)
    }
}
