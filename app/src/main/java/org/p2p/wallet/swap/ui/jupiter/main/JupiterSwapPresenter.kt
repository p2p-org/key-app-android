package org.p2p.wallet.swap.ui.jupiter.main

import java.math.BigDecimal
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
    import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.statemanager.SwapFeatureException
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.swap.jupiter.statemanager.price_impact.SwapPriceImpact
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapButtonMapper
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapWidgetMapper
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel

private const val AMOUNT_INPUT_DELAY = 400L

class JupiterSwapPresenter(
    private val managerHolder: SwapStateManagerHolder,
    private val stateManager: SwapStateManager,
    private val widgetMapper: SwapWidgetMapper,
    private val buttonMapper: SwapButtonMapper,
    private val rateLoaderTokenA: SwapTokenRateLoader,
    private val rateLoaderTokenB: SwapTokenRateLoader,
    private val dispatchers: CoroutineDispatchers,
) : BasePresenter<JupiterSwapContract.View>(), JupiterSwapContract.Presenter {

    private var needToScrollPriceImpact = true
    private var featureState: SwapState? = null
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
        debounceInputJob?.cancel()
        cancelRateJobs()
        debounceInputJob = launch {
            val newAmount = amount.toBigDecimalOrZero()
            val action = if (newAmount.isZero()) {
                SwapStateAction.EmptyAmountTokenA
            } else {
                val pair = featureState?.getTokensPair()
                val tokenA = pair?.first
                val tokenB = pair?.second
                if (tokenB != null) {
                    view?.setSecondTokenWidgetState(widgetMapper.mapTokenBLoading(token = tokenB))
                }
                if (tokenA != null) {
                    getRateTokenA(widgetAModel = widgetAState, tokenA = tokenA, tokenAmount = newAmount)
                }
                stateManager.onNewAction(SwapStateAction.CancelSwapLoading)
                delay(AMOUNT_INPUT_DELAY)
                SwapStateAction.TokenAAmountChanged(newAmount)
            }
            stateManager.onNewAction(action)
        }
    }

    private fun cancelRateJobs() {
        rateTokenAJob?.cancel()
        rateTokenBJob?.cancel()
    }

    private fun SwapState.getTokensPair(): Pair<SwapTokenModel?, SwapTokenModel?> {
        return when (this) {
            SwapState.InitialLoading -> null to null
            is SwapState.LoadingRoutes -> tokenA to tokenB
            is SwapState.LoadingTransaction -> tokenA to tokenB
            is SwapState.SwapException -> previousFeatureState.getTokensPair()
            is SwapState.SwapLoaded -> tokenA to tokenB
            is SwapState.TokenAZero -> tokenA to tokenB
        }
    }

    override fun onSwapTokenClick() {
        stateManager.onNewAction(SwapStateAction.SwapSuccess)
    }

    override fun onAllAmountClick() {
        val allTokenAAmount = when (val featureState = featureState) {
            SwapState.InitialLoading,
            is SwapState.SwapLoaded,
            is SwapState.TokenAZero,
            is SwapState.LoadingRoutes,
            is SwapState.LoadingTransaction -> getTokenAAmount(featureState)
            is SwapState.SwapException -> getTokenAAmount(featureState.previousFeatureState)
            null -> null
        }
        if (allTokenAAmount != null) {
            cancelRateJobs()
            stateManager.onNewAction(SwapStateAction.TokenAAmountChanged(allTokenAAmount))
        }
    }

    private fun getTokenAAmount(state: SwapState): BigDecimal? {
        val tokenA = when (state) {
            is SwapState.LoadingRoutes -> state.tokenA
            is SwapState.LoadingTransaction -> state.tokenA
            is SwapState.SwapLoaded -> state.tokenA
            is SwapState.TokenAZero -> state.tokenA
            SwapState.InitialLoading,
            is SwapState.SwapException.FeatureExceptionWrapper,
            is SwapState.SwapException.OtherException -> null
        }
        return (tokenA as? SwapTokenModel.UserToken)?.details?.total
    }

    override fun onChangeTokenAClick() {
        if (isChangeTokenScreenAvailable(featureState)) {
            view?.openChangeTokenAScreen()
        }
    }

    override fun onChangeTokenBClick() {
        if (isChangeTokenScreenAvailable(featureState)) {
            view?.openChangeTokenBScreen()
        }
    }

    private fun isChangeTokenScreenAvailable(featureState: SwapState?): Boolean {
        return when (featureState) {
            null,
            SwapState.InitialLoading -> false
            is SwapState.LoadingRoutes,
            is SwapState.LoadingTransaction,
            is SwapState.SwapLoaded,
            is SwapState.TokenAZero -> true
            is SwapState.SwapException ->
                isChangeTokenScreenAvailable(featureState.previousFeatureState)
        }
    }

    override fun onBackPressed() {
        view?.closeScreen()
    }

    override fun finishFeature(stateManagerHolderKey: String) {
        managerHolder.clear(stateManagerHolderKey)
    }

    private fun handleFeatureState(state: SwapState) {
        cancelRateJobs()
        featureState = state
        when (state) {
            is SwapState.InitialLoading -> handleInitialLoading(state)
            is SwapState.TokenAZero -> handleTokenAZero(state)
            is SwapState.LoadingRoutes -> handleLoadingRoutes(state)
            is SwapState.LoadingTransaction -> handleLoadingTransaction(state)
            is SwapState.SwapLoaded -> handleSwapLoaded(state)
            is SwapState.SwapException.FeatureExceptionWrapper -> handleFeatureException(state)
            is SwapState.SwapException.OtherException -> {
                // todo
            }
        }
    }

    private fun handleFeatureException(state: SwapState.SwapException.FeatureExceptionWrapper) {
        val (widgetAState, widgetBState) = mapWidgetStates(state.previousFeatureState)
        when (val featureException = state.featureException) {
            is SwapFeatureException.SameTokens ->
                view?.setButtonState(buttonState = buttonMapper.mapSameToken())
            is SwapFeatureException.RoutesNotFound ->
                view?.setButtonState(buttonState = buttonMapper.mapRouteNotFound())
            is SwapFeatureException.NotValidTokenA -> {
                val tokenA = state.previousFeatureState.getTokensPair().first
                this.widgetAState = widgetMapper.mapErrorTokenAAmount(
                    tokenA = tokenA,
                    oldWidgetAState = widgetAState,
                    notValidAmount = featureException.notValidAmount
                )
                if (tokenA != null) getRateTokenA(
                    widgetAModel = this.widgetAState,
                    tokenA = tokenA,
                    tokenAmount = featureException.notValidAmount
                )
                view?.setButtonState(buttonState = buttonMapper.mapTokenAmountNotEnough(tokenA))
            }
        }
        updateWidgets()
    }

    private fun handleSwapLoaded(state: SwapState.SwapLoaded) {
        mapWidgetStates(state)
        updateWidgets()
        view?.setButtonState(
            buttonState = buttonMapper.mapReadyToSwap(tokenA = state.tokenA, tokenB = state.tokenB)
        )
        getRateTokenA(widgetAModel = widgetAState, tokenA = state.tokenA, tokenAmount = state.amountTokenA)
        getRateTokenB(widgetBModel = widgetBState, tokenB = state.tokenB, tokenAmount = state.amountTokenB)
    }

    private fun handleLoadingTransaction(state: SwapState.LoadingTransaction) {
        mapWidgetStates(state)
        updateWidgets()
        view?.setButtonState(buttonState = buttonMapper.mapLoading())
        getRateTokenA(widgetAModel = widgetAState, tokenA = state.tokenA, tokenAmount = state.amountTokenA)
        getRateTokenB(widgetBModel = widgetBState, tokenB = state.tokenB, tokenAmount = state.amountTokenB)
    }

    private fun handleLoadingRoutes(state: SwapState.LoadingRoutes) {
        mapWidgetStates(state)
        updateWidgets()
        view?.setButtonState(buttonState = buttonMapper.mapLoading())
        getRateTokenA(widgetAModel = widgetAState, tokenA = state.tokenA, tokenAmount = state.amountTokenA)
    }

    private fun handleTokenAZero(state: SwapState.TokenAZero) {
        mapWidgetStates(state)
        updateWidgets()
        view?.setButtonState(buttonMapper.mapEnterAmount())
    }

    private fun handleInitialLoading(state: SwapState.InitialLoading) {
        mapWidgetStates(state)
        updateWidgets()
        view?.setButtonState(buttonState = SwapButtonState.Hide)
    }

    private fun mapWidgetStates(state: SwapState): Pair<SwapWidgetModel, SwapWidgetModel> {
        val result = when (state) {
            SwapState.InitialLoading ->
                widgetMapper.mapWidgetLoading(tokenType = SwapTokenType.TOKEN_A) to
                    widgetMapper.mapWidgetLoading(tokenType = SwapTokenType.TOKEN_B)
            is SwapState.LoadingRoutes ->
                widgetMapper.mapTokenAAndSaveOldFiatAmount(
                    oldWidgetModel = widgetAState,
                    token = state.tokenA,
                    tokenAmount = state.amountTokenA
                ) to widgetMapper.mapTokenBLoading(token = state.tokenB)
            is SwapState.LoadingTransaction ->
                widgetMapper.mapTokenAAndSaveOldFiatAmount(
                    oldWidgetModel = widgetAState,
                    token = state.tokenA,
                    tokenAmount = state.amountTokenA
                ) to widgetMapper.mapTokenBAndSaveOldFiatAmount(
                    oldWidgetModel = widgetBState,
                    token = state.tokenB,
                    tokenAmount = state.amountTokenB,
                )
            is SwapState.SwapLoaded ->
                widgetMapper.mapTokenAAndSaveOldFiatAmount(
                    oldWidgetModel = widgetAState,
                    token = state.tokenA,
                    tokenAmount = state.amountTokenA
                ) to widgetMapper.mapTokenBAndSaveOldFiatAmount(
                    oldWidgetModel = widgetBState,
                    token = state.tokenB,
                    tokenAmount = state.amountTokenB,
                )
            is SwapState.TokenAZero ->
                widgetMapper.mapTokenA(token = state.tokenA, tokenAmount = null) to
                    widgetMapper.mapTokenB(token = state.tokenB, tokenAmount = null)

            is SwapState.SwapException -> mapWidgetStates(state.previousFeatureState)
        }
        widgetAState = result.first
        widgetBState = result.second
        return result
    }

    private fun getRateTokenA(widgetAModel: SwapWidgetModel, tokenA: SwapTokenModel, tokenAmount: BigDecimal) {
        rateTokenAJob?.cancel()
        rateTokenAJob = rateLoaderTokenA.getRate(tokenA)
            .flowOn(dispatchers.io)
            .onEach {
                if (isActive) handleRateTokenALoader(
                    widgetAModel = widgetAModel,
                    state = it,
                    tokenAmount = tokenAmount,
                )
            }
            .launchIn(this)
    }

    private fun getRateTokenB(widgetBModel: SwapWidgetModel, tokenB: SwapTokenModel, tokenAmount: BigDecimal) {
        rateTokenBJob?.cancel()
        rateTokenBJob = rateLoaderTokenB.getRate(tokenB)
            .flowOn(dispatchers.io)
            .onEach {
                if (isActive) handleRateTokenBLoader(
                    widgetBModel = widgetBModel,
                    state = it,
                    tokenAmount = tokenAmount,
                )
            }
            .launchIn(this)
    }

    private fun handleRateTokenALoader(
        widgetAModel: SwapWidgetModel,
        state: SwapRateLoaderState,
        tokenAmount: BigDecimal,
    ) {
        val newWidgetModel = widgetMapper.mapFiatAmount(
            state = state,
            oldWidgetModel = widgetAModel,
            tokenAmount = tokenAmount
        )
        widgetAState = newWidgetModel
        view?.setFirstTokenWidgetState(state = widgetAState)
    }

    private fun handleRateTokenBLoader(
        widgetBModel: SwapWidgetModel,
        state: SwapRateLoaderState,
        tokenAmount: BigDecimal,
    ) {

        var newWidgetModel = widgetMapper.mapFiatAmount(
            state = state,
            oldWidgetModel = widgetBModel,
            tokenAmount = tokenAmount
        )
        when (val priceImpact = getPriceImpact(featureState)?.toPriceImpactType()) {
            null,
            SwapPriceImpact.NORMAL -> view?.showPriceImpact(SwapPriceImpact.NORMAL)
            SwapPriceImpact.YELLOW,
            SwapPriceImpact.RED -> {
                newWidgetModel = widgetMapper.mapPriceImpact(newWidgetModel, priceImpact)
                view?.showPriceImpact(priceImpact)
                if (needToScrollPriceImpact) {
                    view?.scrollToPriceImpact()
                    needToScrollPriceImpact = false
                }
            }
        }
        widgetBState = newWidgetModel
        view?.setSecondTokenWidgetState(state = widgetBState)
    }

    private fun getPriceImpact(state: SwapState?): BigDecimal? {
        return when (state) {
            null,
            SwapState.InitialLoading,
            is SwapState.LoadingRoutes,
            is SwapState.TokenAZero -> null
            is SwapState.SwapException -> getPriceImpact(state.previousFeatureState)

            is SwapState.LoadingTransaction -> state.routes.getOrNull(state.activeRoute)?.priceImpactPct
            is SwapState.SwapLoaded -> state.routes.getOrNull(state.activeRoute)?.priceImpactPct
        }
    }

    private fun updateWidgets() {
        view?.setFirstTokenWidgetState(state = widgetAState)
        view?.setSecondTokenWidgetState(state = widgetBState)
    }

    private val threePercent
        get() = BigDecimal.valueOf(0.3)

    private val onePercent
        get() = BigDecimal.valueOf(0.1)

    private fun BigDecimal.toPriceImpactType(): SwapPriceImpact {
        return when {
            isLessThan(onePercent) -> SwapPriceImpact.NORMAL
            isLessThan(threePercent) -> SwapPriceImpact.YELLOW
            else -> SwapPriceImpact.RED
        }
    }
}
