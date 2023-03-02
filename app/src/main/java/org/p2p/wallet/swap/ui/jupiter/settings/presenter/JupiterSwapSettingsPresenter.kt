package org.p2p.wallet.swap.ui.jupiter.settings.presenter

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.ui.jupiter.settings.JupiterSwapSettingsContract

class JupiterSwapSettingsPresenter(
    private val stateManager: SwapStateManager,
    private val emptyMapper: SwapEmptySettingsMapper,
    private val loadingMapper: SwapLoadingSettingsMapper,
    private val commonMapper: SwapCommonSettingsMapper,
) : BasePresenter<JupiterSwapSettingsContract.View>(), JupiterSwapSettingsContract.Presenter {

    private var featureState: SwapState? = null

    override fun attach(view: JupiterSwapSettingsContract.View) {
        super.attach(view)

        stateManager.observe()
            .onEach(::handleFeatureState)
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
            is SwapState.LoadingRoutes -> view?.bindSettingsList(
                list = loadingMapper.mapLoadingList(slippage = state.slippage)
            )
            is SwapState.LoadingTransaction -> {
                // todo
            }
            is SwapState.SwapLoaded -> {
                // todo
            }
            is SwapState.SwapException -> handleFeatureState(state.previousFeatureState)
        }
    }

    override fun onSettingItemClick(item: FinanceBlockCellModel) {
        val payload = item.payload ?: return
        // todo
    }
}
