package org.p2p.wallet.swap.ui.jupiter.main

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapButtonMapper
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapWidgetMapper
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class JupiterSwapPresenter(
    private val stateManager: SwapStateManager,
    private val dispatchers: CoroutineDispatchers,
    private val widgetMapper: SwapWidgetMapper,
    private val buttonMapper: SwapButtonMapper,
) : BasePresenter<JupiterSwapContract.View>(), JupiterSwapContract.Presenter {

    override fun attach(view: JupiterSwapContract.View) {
        super.attach(view)

        stateManager.observe()
            .onEach(::handleFeatureState)
            .flowOn(dispatchers.io)
            .launchIn(this)
    }

    override fun switchTokens() {
        stateManager.onNewAction(SwapStateAction.SwitchTokens)
    }

    override fun onTokenAmountChange(amount: String) {
        stateManager.onNewAction(SwapStateAction.TokenAAmountChanged(amount.toBigDecimal()))
    }

    override fun onSwapTokenClick() {
        stateManager.onNewAction(SwapStateAction.ConfirmTokenChanged)
    }

    private fun handleFeatureState(state: SwapState) {
        when (state) {
            SwapState.InitialLoading -> handleInitialLoading()
            is SwapState.TokenAZero -> handleTokenAZero(state)
            is SwapState.LoadingRoutes -> handleLoadingRoutes(state)
            is SwapState.LoadingTransaction -> handleLoadingTransaction(state)
            is SwapState.SwapLoaded -> handleSwapLoaded(state)
        }
    }

    private fun handleSwapLoaded(state: SwapState.SwapLoaded) {
        view?.setFirstTokenWidgetState(
            state = widgetMapper.mapTokenA(token = state.tokenA, tokenAmount = state.amountTokenA)
        )
        view?.setSecondTokenWidgetState(
            state = widgetMapper.mapTokenB(token = state.tokenB, tokenAmount = state.amountTokenB)
        )
        view?.setButtonState(
            buttonState = buttonMapper.mapReadyToSwap(tokenA = state.tokenA, tokenB = state.tokenB)
        )
    }

    private fun handleLoadingTransaction(state: SwapState.LoadingTransaction) {
        view?.setFirstTokenWidgetState(
            state = widgetMapper.mapTokenA(token = state.tokenA, tokenAmount = state.amountTokenA)
        )
        view?.setSecondTokenWidgetState(
            state = widgetMapper.mapTokenB(token = state.tokenB, tokenAmount = state.amountTokenB)
        )
        view?.setButtonState(buttonState = buttonMapper.mapLoading())
    }

    private fun handleLoadingRoutes(state: SwapState.LoadingRoutes) {
        view?.setFirstTokenWidgetState(
            state = widgetMapper.mapTokenA(token = state.tokenA, tokenAmount = state.amountTokenA)
        )
        view?.setSecondTokenWidgetState(state = widgetMapper.mapTokenBLoading(token = state.tokenB))
        view?.setButtonState(buttonState = buttonMapper.mapLoading())
    }

    private fun handleTokenAZero(state: SwapState.TokenAZero) {
        view?.setFirstTokenWidgetState(
            state = widgetMapper.mapTokenA(token = state.tokenA, tokenAmount = null)
        )
        view?.setSecondTokenWidgetState(
            state = widgetMapper.mapTokenB(token = state.tokenB, tokenAmount = null)
        )
        view?.setButtonState(buttonMapper.mapEnterAmount())
    }

    private fun handleInitialLoading() {
        view?.setFirstTokenWidgetState(state = widgetMapper.mapWidgetLoading(isTokenA = true))
        view?.setSecondTokenWidgetState(state = widgetMapper.mapWidgetLoading(isTokenA = false))
        view?.setButtonState(buttonState = SwapButtonState.Hide)
    }
}
