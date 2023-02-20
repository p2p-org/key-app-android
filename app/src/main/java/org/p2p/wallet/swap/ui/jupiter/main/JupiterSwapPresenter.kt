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
            SwapState.InitialLoading -> {
                view?.setFirstTokenWidgetState(widgetMapper.mapWidgetLoading(isTokenA = true))
                view?.setSecondTokenWidgetState(widgetMapper.mapWidgetLoading(isTokenA = false))
                view?.setButtonState(SwapButtonState.Hide)
            }
            is SwapState.TokenAZero -> {
                view?.setFirstTokenWidgetState(widgetMapper.mapTokenA(state.tokenA, tokenAmount = null))
                view?.setSecondTokenWidgetState(widgetMapper.mapTokenB(state.tokenB, tokenAmount = null))
                view?.setButtonState(buttonMapper.mapEnterAmount())
            }
            is SwapState.LoadingRoutes -> {
                view?.setFirstTokenWidgetState(widgetMapper.mapTokenA(state.tokenA, state.amountTokenA))
                view?.setSecondTokenWidgetState(widgetMapper.mapTokenBLoading(state.tokenB))
                view?.setButtonState(buttonMapper.mapLoading())
            }
            is SwapState.LoadingTransaction -> {
                view?.setFirstTokenWidgetState(widgetMapper.mapTokenA(state.tokenA, state.amountTokenA))
                view?.setSecondTokenWidgetState(widgetMapper.mapTokenB(state.tokenB, state.amountTokenB))
                view?.setButtonState(buttonMapper.mapLoading())
            }
            is SwapState.SwapLoaded -> {
                view?.setFirstTokenWidgetState(widgetMapper.mapTokenA(state.tokenA, state.amountTokenA))
                view?.setSecondTokenWidgetState(widgetMapper.mapTokenB(state.tokenB, state.amountTokenB))
                view?.setButtonState(buttonMapper.mapReadyToSwap(state.tokenA, state.tokenB))
            }
        }
    }
}
