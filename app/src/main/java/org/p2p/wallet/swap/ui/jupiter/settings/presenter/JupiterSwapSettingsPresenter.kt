package org.p2p.wallet.swap.ui.jupiter.settings.presenter

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.ui.jupiter.settings.JupiterSwapSettingsContract

class JupiterSwapSettingsPresenter(
    private val stateManager: SwapStateManager,
    private val emptyMapper: SwapEmptySettingsMapper,
    private val loadingMapper: SwapLoadingSettingsMapper,
    private val contentMapper: SwapContentSettingsMapper,
    private val commonMapper: SwapCommonSettingsMapper,
    private val swapTokensRepository: JupiterSwapTokensRepository,
) : BasePresenter<JupiterSwapSettingsContract.View>(), JupiterSwapSettingsContract.Presenter {

    private var featureState: SwapState? = null

    override fun attach(view: JupiterSwapSettingsContract.View) {
        super.attach(view)

        val jupiterTokens = flow { emit(swapTokensRepository.getTokens()) }
        stateManager.observe()
            .combine(jupiterTokens) { state: SwapState, tokens: List<JupiterSwapToken> -> state to tokens }
            .onEach { handleFeatureState(it.first, it.second) }
            .launchIn(this)
    }

    private fun handleFeatureState(state: SwapState, tokens: List<JupiterSwapToken>) {
        featureState = state
        val contentList = getContentListByFeatureState(state, tokens)
        view?.bindSettingsList(contentList)
    }

    override fun onSettingItemClick(item: FinanceBlockCellModel) {
        val payload = item.payload ?: return
        // todo
    }

    override fun onCustomSlippageChange(slippage: Double?) {
        TODO("Not yet implemented")
    }

    private fun SwapState.getCurrentSlippage(): Slippage {
        return when (this) {
            SwapState.InitialLoading -> SwapStateManager.DEFAULT_SLIPPAGE
            is SwapState.LoadingRoutes -> slippage
            is SwapState.LoadingTransaction -> slippage
            is SwapState.SwapLoaded -> slippage
            is SwapState.TokenAZero -> slippage
            is SwapState.SwapException -> previousFeatureState.getCurrentSlippage()
        }
    }

    private fun getContentListByFeatureState(state: SwapState, tokens: List<JupiterSwapToken>): List<AnyCellItem> {
        return when (state) {
            SwapState.InitialLoading -> listOf()
            is SwapState.TokenAZero -> emptyMapper.mapEmptyList(
                tokenB = state.tokenB
            )
            is SwapState.LoadingRoutes ->
                loadingMapper.mapLoadingList()
            is SwapState.LoadingTransaction -> {
                contentMapper.mapForLoadingTransactionState(
                    slippage = state.slippage,
                    routes = state.routes,
                    activeRoute = state.activeRoute,
                    jupiterTokens = tokens,
                    tokenB = state.tokenB,
                )
            }
            is SwapState.SwapLoaded -> {
                contentMapper.mapForSwapLoadedState(
                    slippage = state.slippage,
                    routes = state.routes,
                    activeRoute = state.activeRoute,
                    jupiterTokens = tokens,
                    tokenBAmount = state.amountTokenB,
                    tokenB = state.tokenB,
                )
            }
            is SwapState.SwapException -> getContentListByFeatureState(state.previousFeatureState, tokens)
        }
    }
}
