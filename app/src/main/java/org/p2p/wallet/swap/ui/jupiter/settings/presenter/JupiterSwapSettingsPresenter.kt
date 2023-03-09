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
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.swap.jupiter.analytics.JupiterSwapSettingsAnalytics
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.jupiter.statemanager.rate.SwapRateTickerManager
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.jupiter.SwapRateTickerState
import org.p2p.wallet.swap.ui.jupiter.info.SwapInfoType
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapRateTickerMapper
import org.p2p.wallet.swap.ui.jupiter.settings.JupiterSwapSettingsContract

private const val AMOUNT_INPUT_DELAY = 400L

class JupiterSwapSettingsPresenter(
    private val stateManager: SwapStateManager,
    private val emptyMapper: SwapEmptySettingsMapper,
    private val loadingMapper: SwapLoadingSettingsMapper,
    private val contentMapper: SwapContentSettingsMapper,
    private val commonMapper: SwapCommonSettingsMapper,
    private val rateTickerMapper: SwapRateTickerMapper,
    private val rateTickerManager: SwapRateTickerManager,
    private val analytics: JupiterSwapSettingsAnalytics,
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

        rateTickerManager.observe()
            .onEach(::handleRateTickerChanges)
            .launchIn(this)
    }

    private suspend fun handleFeatureState(state: SwapState, tokens: List<JupiterSwapToken>) {
        featureState = state
        if (isSelectedCustom == null) isSelectedCustom = state.getCurrentSlippage() is Slippage.Custom
        val contentList = getContentListByFeatureState(state, tokens)
            .addSlippageSettings(state)
        view?.bindSettingsList(contentList)
        when (state) {
            is SwapState.LoadingRoutes -> {
                rateTickerManager.handleRoutesLoading(state)
            }
            is SwapState.SwapLoaded -> {
                rateTickerManager.handleJupiterRates(state)
            }
            is SwapState.SwapException -> {
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            SwapState.InitialLoading,
            is SwapState.LoadingTransaction,
            is SwapState.TokenAZero -> Unit
        }
    }

    override fun onSettingItemClick(item: FinanceBlockCellModel) {
        val payload = item.payload ?: return
        when (payload) {
            SwapSlippagePayload.ZERO_POINT_ONE -> {
                isSelectedCustom = false
                analytics.logSlippageChangedClicked(Slippage.Min)
                stateManager.onNewAction(SwapStateAction.SlippageChanged(Slippage.Min))
            }
            SwapSlippagePayload.ZERO_POINT_FIVE -> {
                isSelectedCustom = false
                analytics.logSlippageChangedClicked(Slippage.Medium)
                stateManager.onNewAction(SwapStateAction.SlippageChanged(Slippage.Medium))
            }
            SwapSlippagePayload.ONE -> {
                isSelectedCustom = false
                analytics.logSlippageChangedClicked(Slippage.One)
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
            is SwapSettingsPayload -> {
                onDetailsClick(payload)
            }
        }
    }

    private fun onDetailsClick(settingsPayload: SwapSettingsPayload) {
        when (settingsPayload) {
            SwapSettingsPayload.ROUTE ->
                if (canOpenDetails()) view?.showRouteDialog()
            SwapSettingsPayload.NETWORK_FEE ->
                view?.showDetailsDialog(SwapInfoType.NETWORK_FEE)
            SwapSettingsPayload.CREATION_FEE ->
                view?.showDetailsDialog(SwapInfoType.ACCOUNT_FEE)
            SwapSettingsPayload.LIQUIDITY_FEE ->
                if (canOpenDetails()) view?.showDetailsDialog(SwapInfoType.LIQUIDITY_FEE)
            SwapSettingsPayload.MINIMUM_RECEIVED ->
                view?.showDetailsDialog(SwapInfoType.MINIMUM_RECEIVED)
            SwapSettingsPayload.ESTIMATED_FEE ->
                Unit
        }
    }

    private fun canOpenDetails(): Boolean {
        return featureState is SwapState.SwapLoaded || featureState is SwapState.LoadingTransaction
    }

    override fun onCustomSlippageChange(slippage: Double?) {
        debounceInputJob?.cancel()
        if (slippage == null) {
            return
        }
        val newCustomSlippage = Slippage.parse(slippage)
        debounceInputJob = launch {
            stateManager.onNewAction(SwapStateAction.CancelSwapLoading)
            analytics.logSlippageChangedClicked(newCustomSlippage)
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
            is SwapState.TokenANotZero -> slippage
            is SwapState.SwapException -> previousFeatureState.getCurrentSlippage()
        }
    }

    private suspend fun getContentListByFeatureState(
        state: SwapState,
        tokens: List<JupiterSwapToken>
    ): List<AnyCellItem> {
        return when (state) {
            SwapState.InitialLoading -> {
                emptyList()
            }
            is SwapState.TokenAZero -> {
                emptyMapper.mapEmptyList(tokenB = state.tokenB)
            }
            is SwapState.TokenANotZero -> {
                emptyMapper.mapEmptyList(tokenB = state.tokenB)
            }
            is SwapState.LoadingRoutes -> {
                loadingMapper.mapLoadingList()
            }
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
            is SwapState.SwapException -> {
                getContentListByFeatureState(state.previousFeatureState, tokens)
            }
        }
    }

    private fun handleRateTickerChanges(state: SwapRateTickerState) {
        when (state) {
            is SwapRateTickerState.Shown -> view?.setRatioState(rateTickerMapper.mapRateLoaded(state))
            is SwapRateTickerState.Loading -> view?.setRatioState(rateTickerMapper.mapRateSkeleton(state))
            is SwapRateTickerState.Hidden -> view?.setRatioState(state = null)
        }
    }

    private fun List<AnyCellItem>.addSlippageSettings(state: SwapState): List<AnyCellItem> {
        val currentSlippage = state.getCurrentSlippage()
        return this + commonMapper.mapSlippageList(
            slippage = currentSlippage,
            isSelectedCustom = isSelectedCustom ?: false
        )
    }
}
