package org.p2p.wallet.swap.ui.jupiter.main

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapWidgetMapper
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class JupiterSwapPresenter(
    private val stateManager: SwapStateManager,
    private val dispatchers: CoroutineDispatchers,
    private val widgetMapper: SwapWidgetMapper,
) : BasePresenter<JupiterSwapContract.View>(), JupiterSwapContract.Presenter {

    override fun attach(view: JupiterSwapContract.View) {
        super.attach(view)

        stateManager.observe()
            .onEach(::handleFeatureState)
            .flowOn(dispatchers.io)
            .launchIn(this)
    }

    private fun handleFeatureState(state: SwapState) {
        when (state) {
            SwapState.InitialLoading -> {
                view?.setFirstTokenWidgetState(widgetMapper.mapWidgetLoading(isTokenA = true))
                view?.setSecondTokenWidgetState(widgetMapper.mapWidgetLoading(isTokenA = false))
            }
            is SwapState.TokenAZero -> {
                view?.setFirstTokenWidgetState(widgetMapper.mapTokenA(state.tokenA, tokenAmount = null))
                view?.setSecondTokenWidgetState(widgetMapper.mapTokenB(state.tokenB, tokenAmount = null))
            }
            is SwapState.LoadingRoutes -> {
                view?.setFirstTokenWidgetState(widgetMapper.mapTokenA(state.tokenA, state.amountTokenA))
                view?.setSecondTokenWidgetState(widgetMapper.mapTokenBLoading(state.tokenB))
            }
            is SwapState.LoadingTransaction -> {
                view?.setFirstTokenWidgetState(widgetMapper.mapTokenA(state.tokenA, state.amountTokenA))
                view?.setSecondTokenWidgetState(widgetMapper.mapTokenB(state.tokenB, state.amountTokenB))
            }
            is SwapState.SwapLoaded -> {
                view?.setFirstTokenWidgetState(widgetMapper.mapTokenA(state.tokenA, state.amountTokenA))
                view?.setSecondTokenWidgetState(widgetMapper.mapTokenB(state.tokenB, state.amountTokenB))
            }
        }
    }
}
