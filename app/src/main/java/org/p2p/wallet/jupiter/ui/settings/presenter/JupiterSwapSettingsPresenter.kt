package org.p2p.wallet.jupiter.ui.settings.presenter

import timber.log.Timber
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.jupiter.analytics.JupiterSwapSettingsAnalytics
import org.p2p.wallet.jupiter.model.SwapRateTickerState
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.jupiter.statemanager.rate.SwapRateTickerManager
import org.p2p.wallet.jupiter.ui.info.SwapInfoType
import org.p2p.wallet.jupiter.ui.main.mapper.SwapRateTickerMapper
import org.p2p.wallet.jupiter.ui.settings.JupiterSwapSettingsContract
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.Slippage.Companion.PERCENT_DIVIDE_VALUE

private val AMOUNT_INPUT_DELAY = 1.seconds

@OptIn(ExperimentalCoroutinesApi::class)
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

    private var debounceInputJob: Job? = null
    private var isCustomSlippageSelected: Boolean? = null
    private var currentContentList = listOf<AnyCellItem>()
        get() = field.addSlippageSettings(featureState)

    override fun attach(view: JupiterSwapSettingsContract.View) {
        super.attach(view)

        stateManager.observe()
            .mapLatest(::handleFeatureState)
            .launchIn(this)

        rateTickerManager.observe()
            .onEach(::handleRateTickerChanges)
            .launchIn(this)

        stateManager.resume()
    }

    private suspend fun handleFeatureState(state: SwapState) {
        featureState = state
        if (isCustomSlippageSelected == null) {
            isCustomSlippageSelected = state.getCurrentSlippage() is Slippage.Custom
        }
        val contentList = getContentListByFeatureState(state)
        currentContentList = contentList
        view?.bindSettingsList(currentContentList)
        val jupiterSolToken = swapTokensRepository.requireWrappedSol()
        when (state) {
            is SwapState.LoadingRoutes -> {
                rateTickerManager.handleRoutesLoading(state)
            }
            is SwapState.SwapLoaded -> {
                rateTickerManager.handleJupiterRates(state)
                val solToken = swapTokensRepository.requireWrappedSol()
                currentContentList = contentMapper.mapForSwapLoadedState(
                    state = state,
                    solTokenForFee = solToken,
                )
            }
            is SwapState.LoadingTransaction -> {
                currentContentList = contentMapper.mapForLoadingTransactionState(
                    state = state,
                    solTokenForFee = jupiterSolToken,
                )
            }
            is SwapState.RoutesLoaded -> {
                rateTickerManager.handleJupiterRates(state)
                currentContentList = contentMapper.mapForLoadingTransactionState(
                    state = state,
                    solTokenForFee = jupiterSolToken,
                )
            }
            is SwapState.SwapException -> {
                val previousState = state.previousFeatureState
                if (previousState is SwapState.RoutesLoaded) {
                    currentContentList = contentMapper.mapForRoutesLoadedState(
                        state = previousState,
                        solTokenForFee = jupiterSolToken,
                    )
                } else {
                    rateTickerManager.handleSwapException(state)
                }
            }
            SwapState.InitialLoading,
            is SwapState.TokenAZero,
            is SwapState.TokenANotZero -> Unit
        }
        view?.bindSettingsList(currentContentList)
    }

    override fun onSettingItemClick(item: MainCellModel) {
        val payload = item.payload ?: return
        when (payload) {
            SwapSlippagePayload.ZERO_POINT_ONE -> {
                isCustomSlippageSelected = false
                analytics.logSlippageChangedClicked(Slippage.Min)
                stateManager.onNewAction(SwapStateAction.SlippageChanged(Slippage.Min))
            }
            SwapSlippagePayload.ZERO_POINT_FIVE -> {
                isCustomSlippageSelected = false
                analytics.logSlippageChangedClicked(Slippage.Medium)
                stateManager.onNewAction(SwapStateAction.SlippageChanged(Slippage.Medium))
            }
            SwapSlippagePayload.ONE -> {
                isCustomSlippageSelected = false
                analytics.logSlippageChangedClicked(Slippage.One)
                stateManager.onNewAction(SwapStateAction.SlippageChanged(Slippage.One))
            }
            SwapSlippagePayload.CUSTOM -> {
                isCustomSlippageSelected = true
                view?.bindSettingsList(currentContentList)
            }
            is SwapSettingsPayload -> {
                onDetailsClick(payload)
            }
        }
    }

    private fun onDetailsClick(settingsPayload: SwapSettingsPayload) {
        when (settingsPayload) {
            SwapSettingsPayload.ROUTE -> {
                if (canOpenDetails(featureState)) view?.showRouteDialog()
            }
            SwapSettingsPayload.NETWORK_FEE -> {
                view?.showDetailsDialog(SwapInfoType.NETWORK_FEE)
            }
            SwapSettingsPayload.CREATION_FEE -> {
                view?.showDetailsDialog(SwapInfoType.ACCOUNT_FEE)
            }
            SwapSettingsPayload.LIQUIDITY_FEE -> {
                if (canOpenDetails(featureState)) view?.showDetailsDialog(SwapInfoType.LIQUIDITY_FEE)
            }
            SwapSettingsPayload.MINIMUM_RECEIVED -> {
                view?.showDetailsDialog(SwapInfoType.MINIMUM_RECEIVED)
            }
            SwapSettingsPayload.ESTIMATED_FEE -> {
                Unit
            }
            SwapSettingsPayload.TOKEN_2022_INTEREST -> {
                view?.showDetailsDialog(SwapInfoType.TOKEN_2022_INTEREST)
            }
            SwapSettingsPayload.TOKEN_2022_TRANSFER -> {
                view?.showDetailsDialog(SwapInfoType.TOKEN_2022_TRANSFER)
            }
        }
    }

    private fun canOpenDetails(currentState: SwapState): Boolean =
        when (currentState) {
            SwapState.InitialLoading,
            is SwapState.LoadingRoutes,
            is SwapState.TokenANotZero,
            is SwapState.TokenAZero -> false

            is SwapState.LoadingTransaction,
            is SwapState.RoutesLoaded,
            is SwapState.SwapLoaded -> true

            is SwapState.SwapException -> canOpenDetails(currentState.previousFeatureState)
        }

    override fun onCustomSlippageChange(slippage: Double?) {
        debounceInputJob?.cancel()
        if (slippage == null) {
            return
        }
        val newCustomSlippage = Slippage.parse(slippage / PERCENT_DIVIDE_VALUE)
        Timber.i("Jupiter swap slippage changed: ${newCustomSlippage.percentValue}")
        debounceInputJob = launch {
            stateManager.onNewAction(SwapStateAction.CancelSwapLoading)
            delay(AMOUNT_INPUT_DELAY.inWholeMilliseconds)
            analytics.logSlippageChangedClicked(newCustomSlippage)
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
            is SwapState.RoutesLoaded -> slippage
            is SwapState.SwapException -> previousFeatureState.getCurrentSlippage()
        }
    }

    private suspend fun getContentListByFeatureState(
        state: SwapState,
    ): List<AnyCellItem> {
        val jupiterSolToken = swapTokensRepository.requireWrappedSol()
        return when (state) {
            SwapState.InitialLoading -> {
                emptyList()
            }
            is SwapState.TokenAZero -> {
                emptyMapper.mapEmptyList(tokenB = state.tokenB, jupiterSolToken)
            }
            is SwapState.TokenANotZero -> {
                emptyMapper.mapEmptyList(tokenB = state.tokenB, jupiterSolToken)
            }
            is SwapState.LoadingRoutes,
            is SwapState.LoadingTransaction,
            is SwapState.RoutesLoaded,
            is SwapState.SwapLoaded -> {
                loadingMapper.mapLoadingList(jupiterSolToken)
            }
            is SwapState.SwapException -> {
                getContentListByFeatureState(state.previousFeatureState)
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
            isSelectedCustom = isCustomSlippageSelected ?: false
        )
    }
}
