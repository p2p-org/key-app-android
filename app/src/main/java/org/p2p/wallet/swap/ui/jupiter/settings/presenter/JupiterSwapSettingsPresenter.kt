package org.p2p.wallet.swap.ui.jupiter.settings.presenter

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.swap.jupiter.statemanager.rate.SwapRateTickerManager
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.model.jupiter.SwapRateTickerState
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapRateTickerMapper
import org.p2p.wallet.swap.ui.jupiter.settings.JupiterSwapSettingsContract

class JupiterSwapSettingsPresenter(
    private val stateManager: SwapStateManager,
    private val emptyMapper: SwapEmptySettingsMapper,
    private val loadingMapper: SwapLoadingSettingsMapper,
    private val commonMapper: SwapCommonSettingsMapper,
    private val rateTickerMapper: SwapRateTickerMapper,
    private val rateTickerManager: SwapRateTickerManager
) : BasePresenter<JupiterSwapSettingsContract.View>(), JupiterSwapSettingsContract.Presenter {

    private var featureState: SwapState? = null

    override fun attach(view: JupiterSwapSettingsContract.View) {
        super.attach(view)

        stateManager.observe()
            .onEach(::handleFeatureState)
            .launchIn(this)

        rateTickerManager.observe()
            .onEach(::handleRateTickerChanges)
            .launchIn(this)
    }

    private fun handleFeatureState(state: SwapState) {
        featureState = state
        when (state) {
            SwapState.InitialLoading -> Unit
            is SwapState.TokenAZero -> view?.bindSettingsList(
                list = emptyMapper.mapEmptyList(
                    slippage = state.slippage,
                    tokenB = state.tokenB
                )
            )
            is SwapState.LoadingRoutes -> {
                view?.bindSettingsList(loadingMapper.mapLoadingList(slippage = state.slippage))
                rateTickerManager.handleRoutesLoading(state)
            }
            is SwapState.LoadingTransaction -> {
                // todo
            }
            is SwapState.SwapLoaded -> {
                // todo
                rateTickerManager.handleJupiterRates(state)
            }
            is SwapState.SwapException -> handleFeatureState(state.previousFeatureState)
        }
    }

    private fun handleRateTickerChanges(state: SwapRateTickerState) {
        when (state) {
            is SwapRateTickerState.Shown -> view?.setRatioState(rateTickerMapper.mapRateLoaded(state))
            is SwapRateTickerState.Loading -> view?.setRatioState(rateTickerMapper.mapRateSkeleton(state))
            is SwapRateTickerState.Hidden -> view?.setRatioState(state = null)
        }
    }

    override fun onSettingItemClick(item: FinanceBlockCellModel) {
        val payload = item.payload ?: return
        // todo
    }
}
