package org.p2p.wallet.swap.ui.jupiter.main

import android.view.Gravity
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.swap.jupiter.interactor.JupiterSwapInteractor
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.swap.jupiter.statemanager.SwapFeatureException
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.swap.jupiter.statemanager.price_impact.SwapPriceImpact
import org.p2p.wallet.swap.jupiter.statemanager.rate.SwapRateTickerManager
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.jupiter.SwapRateTickerState
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapButtonMapper
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapRateTickerMapper
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapWidgetMapper
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.transaction.model.TransactionStateSwapFailureReason
import org.p2p.wallet.transaction.ui.SwapTransactionBottomSheetData
import org.p2p.wallet.transaction.ui.SwapTransactionBottomSheetToken
import org.p2p.wallet.user.repository.UserLocalRepository

private const val AMOUNT_INPUT_DELAY = 400L

class JupiterSwapPresenter(
    private val managerHolder: SwapStateManagerHolder,
    private val stateManager: SwapStateManager,
    private val widgetMapper: SwapWidgetMapper,
    private val buttonMapper: SwapButtonMapper,
    private val rateTickerMapper: SwapRateTickerMapper,
    private val swapInteractor: JupiterSwapInteractor,
    private val transactionManager: TransactionManager,
    private val rateTickerManager: SwapRateTickerManager,
    private val dispatchers: CoroutineDispatchers,
    private val userLocalRepository: UserLocalRepository
) : BasePresenter<JupiterSwapContract.View>(), JupiterSwapContract.Presenter {

    private var needToScrollPriceImpact = true
    private var currentFeatureState: SwapState? = null
    private var rateTokenAJob: Job? = null
    private var rateTokenBJob: Job? = null
    private var debounceInputJob: Job? = null
    private var widgetAState: SwapWidgetModel = widgetMapper.mapWidgetLoading(tokenType = SwapTokenType.TOKEN_A)
    private var widgetBState: SwapWidgetModel = widgetMapper.mapWidgetLoading(tokenType = SwapTokenType.TOKEN_B)
    private val threePercent
        get() = BigDecimal.valueOf(0.3)

    private val onePercent
        get() = BigDecimal.valueOf(0.1)

    override fun attach(view: JupiterSwapContract.View) {
        super.attach(view)

        stateManager.observe()
            .onEach(::handleNewFeatureState)
            .launchIn(this)

        rateTickerManager.observe()
            .onEach(::handleRateTickerChanges)
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
                val pair = currentFeatureState?.getTokensPair()
                val tokenA = pair?.first
                val tokenB = pair?.second
                if (tokenB != null) {
                    view?.setSecondTokenWidgetState(widgetMapper.mapTokenBLoading(token = tokenB))
                }
                if (tokenA != null) {
                    widgetAState = widgetMapper.copyAmount(
                        oldWidgetModel = widgetAState,
                        token = tokenA,
                        tokenAmount = newAmount
                    )
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

    override fun onSwapSliderClicked() {
        launch {
            val internalTransactionId = UUID.randomUUID().toString()
            val currentState = currentFeatureState as? SwapState.SwapLoaded ?: return@launch
            val transactionDate = Date()
            val tokenBUsdAmount =
                stateManager.getTokenRate(currentState.tokenB)
                    .filterIsInstance<SwapRateLoaderState.Loaded>()
                    .map { it.rate * currentState.amountTokenB }
                    .flowOn(dispatchers.io)
                    .firstOrNull()
                    ?: return@launch

            val progressDetails = SwapTransactionBottomSheetData(
                date = transactionDate,
                amountUsd = tokenBUsdAmount.formatFiat(),
                tokenA = SwapTransactionBottomSheetToken(
                    tokenUrl = currentState.tokenA.iconUrl.orEmpty(),
                    tokenName = currentState.tokenA.tokenName,
                    formattedTokenAmount = currentState.amountTokenA.formatToken(currentState.tokenA.decimals)
                ),
                tokenB = SwapTransactionBottomSheetToken(
                    tokenUrl = currentState.tokenB.iconUrl.orEmpty(),
                    tokenName = currentState.tokenB.tokenName,
                    formattedTokenAmount = currentState.amountTokenB.formatToken(currentState.tokenA.decimals)
                )
            )

            view?.showProgressDialog(internalTransactionId, progressDetails)

            when (val result = swapInteractor.swapTokens(currentState.routes[currentState.activeRoute])) {
                is JupiterSwapInteractor.JupiterSwapTokensResult.Success -> {
                    stateManager.onNewAction(SwapStateAction.CancelSwapLoading)
                    val transactionState = TransactionState.JupiterSwapSuccess
                    transactionManager.emitTransactionState(internalTransactionId, transactionState)
                    view?.showCompleteSlider()
                }
                is JupiterSwapInteractor.JupiterSwapTokensResult.Failure -> {
                    // todo also check for slippage error
                    Timber.e(result, "Failed to swap tokens")
                    val causeFailure = if (result.cause is JupiterSwapInteractor.LowSlippageRpcError) {
                        TransactionStateSwapFailureReason.LowSlippage(currentState.slippage)
                    } else {
                        TransactionStateSwapFailureReason.Unknown(result.message.orEmpty())
                    }
                    val transactionState = TransactionState.JupiterSwapFailed(failure = causeFailure)
                    transactionManager.emitTransactionState(internalTransactionId, transactionState)
                    view?.showDefaultSlider()
                }
            }
        }
    }

    override fun onAllAmountClick() {
        val allTokenAAmount = when (val featureState = currentFeatureState) {
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
            onTokenAmountChange(allTokenAAmount.toPlainString())
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
        if (isChangeTokenScreenAvailable(currentFeatureState)) {
            view?.openChangeTokenAScreen()
        }
    }

    override fun onChangeTokenBClick() {
        if (isChangeTokenScreenAvailable(currentFeatureState)) {
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
        rateTickerManager.stopAll()
    }

    override fun reloadFeature() {
        stateManager.onNewAction(SwapStateAction.InitialLoading)
    }

    override fun changeSlippage(newSlippageValue: Slippage) {
        stateManager.onNewAction(SwapStateAction.SlippageChanged(newSlippageValue))
    }

    private fun handleNewFeatureState(state: SwapState) {
        cancelRateJobs()
        currentFeatureState = state
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

    private fun handleRateTickerChanges(state: SwapRateTickerState) {
        when (state) {
            is SwapRateTickerState.Shown -> view?.setRatioState(rateTickerMapper.mapRateLoaded(state))
            is SwapRateTickerState.Loading -> view?.setRatioState(rateTickerMapper.mapRateSkeleton(state))
            is SwapRateTickerState.Hidden -> view?.setRatioState(state = null)
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
                if (tokenA != null) {
                    getRateTokenA(
                        widgetAModel = this.widgetAState,
                        tokenA = tokenA,
                        tokenAmount = featureException.notValidAmount
                    )
                }
                view?.setButtonState(buttonState = buttonMapper.mapTokenAmountNotEnough(tokenA))
            }
        }
        updateWidgets()
    }

    private fun handleSwapLoaded(state: SwapState.SwapLoaded) {
        rateTickerManager.handleJupiterRates(state)

        showRoutesForDebug(state.routes[state.activeRoute], state.slippage)

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
        rateTickerManager.handleRoutesLoading(state)

        mapWidgetStates(state)
        updateWidgets()
        view?.setButtonState(buttonState = buttonMapper.mapLoading())
        getRateTokenA(widgetAModel = widgetAState, tokenA = state.tokenA, tokenAmount = state.amountTokenA)
    }

    private fun handleTokenAZero(state: SwapState.TokenAZero) {
        rateTickerManager.onInitialTokensSelected(state.tokenA, state.tokenB)

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
        val result: Pair<SwapWidgetModel, SwapWidgetModel> = when (state) {
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
        rateTokenAJob = stateManager.getTokenRate(tokenA)
            .flowOn(dispatchers.io)
            .onEach {
                if (isActive) {
                    handleRateTokenALoader(
                        widgetAModel = widgetAModel,
                        state = it,
                        tokenAmount = tokenAmount,
                    )
                }
            }
            .launchIn(this)
    }

    private fun getRateTokenB(widgetBModel: SwapWidgetModel, tokenB: SwapTokenModel, tokenAmount: BigDecimal) {
        rateTokenBJob?.cancel()
        rateTokenBJob = stateManager.getTokenRate(tokenB)
            .flowOn(dispatchers.io)
            .onEach {
                if (isActive) {
                    handleRateTokenBLoader(
                        widgetBModel = widgetBModel,
                        state = it,
                        tokenAmount = tokenAmount,
                    )
                }
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
        when (val priceImpact = getPriceImpact(currentFeatureState)?.toPriceImpactType()) {
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

    private fun showRoutesForDebug(bestRoute: JupiterSwapRoute, slippage: Slippage) {
        val info = buildString {
            append("Route: ")

            bestRoute.marketInfos.forEachIndexed { index, info ->
                if (index == 0) {
                    val fromTokenData = userLocalRepository.findTokenData(info.inputMint.base58Value) ?: return
                    val toTokenData = userLocalRepository.findTokenData(info.outputMint.base58Value) ?: return
                    append(fromTokenData.symbol)
                    append(" -> ")
                    append(toTokenData.symbol)
                    return@forEachIndexed
                }

                val toTokenData = userLocalRepository.findTokenData(info.outputMint.base58Value) ?: return
                append(" -> ")
                append(toTokenData.symbol)
            }

            appendLine()
            appendLine()
            append("Slippage: ${slippage.percentValue}")
        }

        view?.showDebugInfo(
            TextViewCellModel.Raw(
                text = TextContainer(info),
                gravity = Gravity.CENTER
            )
        )
    }

    private fun BigDecimal.toPriceImpactType(): SwapPriceImpact {
        return when {
            isLessThan(onePercent) -> SwapPriceImpact.NORMAL
            isLessThan(threePercent) -> SwapPriceImpact.YELLOW
            else -> SwapPriceImpact.RED
        }
    }
}
