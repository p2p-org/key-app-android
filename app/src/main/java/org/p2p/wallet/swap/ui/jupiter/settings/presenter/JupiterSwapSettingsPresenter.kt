package org.p2p.wallet.swap.ui.jupiter.settings.presenter

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.ui.jupiter.settings.JupiterSwapSettingsContract

private const val AMOUNT_INPUT_DELAY = 400L

class JupiterSwapSettingsPresenter(
    private val stateManager: SwapStateManager,
    private val emptyMapper: SwapEmptySettingsMapper,
    private val loadingMapper: SwapLoadingSettingsMapper,
    private val contentMapper: SwapContentSettingsMapper,
    private val commonMapper: SwapCommonSettingsMapper,
    private val swapTokensRepository: JupiterSwapTokensRepository,
) : BasePresenter<JupiterSwapSettingsContract.View>(), JupiterSwapSettingsContract.Presenter {

    private var featureState: SwapState = SwapState.InitialLoading

    private var jupiterTokens: List<JupiterSwapToken> = listOf()
    private var debounceInputJob: Job? = null
    private var isSelectedCustom: Boolean? = null

    override fun attach(view: JupiterSwapSettingsContract.View) {
        super.attach(view)

        val jupiterTokens = flow { emit(swapTokensRepository.getTokens()) }
        stateManager.observe()
            .combine(jupiterTokens) { state: SwapState, tokens: List<JupiterSwapToken> ->
                this.jupiterTokens = tokens
                state to tokens
            }
            .onEach { handleFeatureState(it.first, it.second) }
            .launchIn(this)
    }

    private suspend fun handleFeatureState(state: SwapState, tokens: List<JupiterSwapToken>) {
        featureState = state
        if (isSelectedCustom == null) isSelectedCustom = state.getCurrentSlippage() is Slippage.Custom
        val contentList = getContentListByFeatureState(state, tokens)
            .addSlippageSettings(state)
        view?.bindSettingsList(contentList)
    }

    override fun onSettingItemClick(item: FinanceBlockCellModel) {
        val payload = item.payload ?: return
        when (payload) {
            SwapSlippagePayload.ZERO_POINT_ONE -> {
                isSelectedCustom = false
                stateManager.onNewAction(SwapStateAction.SlippageChanged(Slippage.Min))
            }
            SwapSlippagePayload.ZERO_POINT_FIVE -> {
                isSelectedCustom = false
                stateManager.onNewAction(SwapStateAction.SlippageChanged(Slippage.Medium))
            }
            SwapSlippagePayload.ONE -> {
                isSelectedCustom = false
                stateManager.onNewAction(SwapStateAction.SlippageChanged(Slippage.One))
            }
            SwapSlippagePayload.CUSTOM -> {
                isSelectedCustom = true
                launch {
                    val contentList = getContentListByFeatureState(featureState, jupiterTokens)
                        .addSlippageSettings(featureState)
                    view?.bindSettingsList(contentList)
                }
            }
        }
    }

    override fun onCustomSlippageChange(slippage: Double?) {
        debounceInputJob?.cancel()
        if (slippage == null) {
            return
        }
        val newCustomSlippage = Slippage.parse(slippage)
        debounceInputJob = launch {
            stateManager.onNewAction(SwapStateAction.CancelSwapLoading)
            delay(AMOUNT_INPUT_DELAY)
            stateManager.onNewAction(SwapStateAction.SlippageChanged(newCustomSlippage))
        }
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

    private suspend fun getContentListByFeatureState(
        state: SwapState,
        tokens: List<JupiterSwapToken>
    ): List<AnyCellItem> {
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
                    tokenA = state.tokenA,
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
                    tokenA = state.tokenA,
                )
            }
            is SwapState.SwapException -> getContentListByFeatureState(state.previousFeatureState, tokens)
        }
    }

    private fun List<AnyCellItem>.addSlippageSettings(state: SwapState): List<AnyCellItem> {
        val currentSlippage = state.getCurrentSlippage()
        val result = this.toMutableList().also {
            it.addAll(commonMapper.mapSlippageList(currentSlippage, isSelectedCustom ?: false))
        }
        return result
    }
}
