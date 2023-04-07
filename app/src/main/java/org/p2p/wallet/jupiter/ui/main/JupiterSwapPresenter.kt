package org.p2p.wallet.jupiter.ui.main

import android.view.Gravity
import org.threeten.bp.ZonedDateTime
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
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.jupiter.analytics.JupiterSwapMainScreenAnalytics
import org.p2p.wallet.jupiter.interactor.JupiterSwapInteractor
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.model.SwapRateTickerState
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.statemanager.SwapFeatureException
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.jupiter.statemanager.price_impact.SwapPriceImpactView
import org.p2p.wallet.jupiter.statemanager.rate.SwapRateTickerManager
import org.p2p.wallet.jupiter.ui.main.mapper.SwapButtonMapper
import org.p2p.wallet.jupiter.ui.main.mapper.SwapRateTickerMapper
import org.p2p.wallet.jupiter.ui.main.mapper.SwapWidgetMapper
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.ui.orca.SwapOpenedFrom
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.transaction.model.TransactionStateSwapFailureReason
import org.p2p.wallet.transaction.ui.SwapTransactionBottomSheetData
import org.p2p.wallet.transaction.ui.SwapTransactionBottomSheetToken
import org.p2p.wallet.user.repository.UserLocalRepository

private const val AMOUNT_INPUT_DELAY = 700L

class JupiterSwapPresenter(
    private val swapOpenedFrom: SwapOpenedFrom,
    private val managerHolder: SwapStateManagerHolder,
    private val stateManager: SwapStateManager,
    private val widgetMapper: SwapWidgetMapper,
    private val buttonMapper: SwapButtonMapper,
    private val rateTickerMapper: SwapRateTickerMapper,
    private val swapInteractor: JupiterSwapInteractor,
    private val analytics: JupiterSwapMainScreenAnalytics,
    private val transactionManager: TransactionManager,
    private val rateTickerManager: SwapRateTickerManager,
    private val dispatchers: CoroutineDispatchers,
    private val userLocalRepository: UserLocalRepository,
    private val historyInteractor: HistoryInteractor
) : BasePresenter<JupiterSwapContract.View>(), JupiterSwapContract.Presenter {

    private var needToShowKeyboard = true
    private var needToScrollPriceImpact = true
    private var currentFeatureState: SwapState? = null
    private var rateTokenAJob: Job? = null
    private var rateTokenBJob: Job? = null
    private var debounceInputJob: Job? = null
    private var widgetAState: SwapWidgetModel = widgetMapper.mapWidgetLoading(tokenType = SwapTokenType.TOKEN_A)
    private var widgetBState: SwapWidgetModel = widgetMapper.mapWidgetLoading(tokenType = SwapTokenType.TOKEN_B)
    private val threePercent
        get() = BigDecimal.valueOf(0.03)

    private val onePercent
        get() = BigDecimal.valueOf(0.01)

    private var shouldLogScreenOpened = true
    private var retryAction = {}

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
            val pair = currentFeatureState?.getTokensPair()
            val tokenA = pair?.first
            val tokenB = pair?.second
            stateManager.onNewAction(SwapStateAction.CancelSwapLoading)
            val action = if (newAmount.isZero()) {
                SwapStateAction.EmptyAmountTokenA
            } else {
                if (tokenA != null) {
                    widgetAState = widgetMapper.copyAmount(
                        oldWidgetModel = widgetAState,
                        token = tokenA,
                        tokenAmount = newAmount
                    )
                    getRateTokenA(widgetAModel = widgetAState, tokenA = tokenA, tokenAmount = newAmount)
                }

                delay(AMOUNT_INPUT_DELAY)
                SwapStateAction.TokenAAmountChanged(newAmount)
            }
            tokenA?.also { analytics.logChangeTokenAAmountChanged(it, amount) }
            stateManager.onNewAction(action)
        }
    }

    private fun cancelRateJobs() {
        rateTokenAJob?.cancel()
        rateTokenBJob?.cancel()
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

            if (tokenBUsdAmount == null) {
                Timber.i(SwapTokenRateNotFound(currentState.tokenB))
            }

            val progressDetails = SwapTransactionBottomSheetData(
                date = transactionDate,
                formattedAmountUsd = tokenBUsdAmount?.asUsd(),
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

            val swapTransaction = currentState.jupiterSwapTransaction

            when (val result = swapInteractor.swapTokens(swapTransaction)) {
                is JupiterSwapInteractor.JupiterSwapTokensResult.Success -> {
                    analytics.logApproveSwapClicked(
                        tokenA = currentState.tokenA,
                        tokenB = currentState.tokenB,
                        tokenAAmount = currentState.amountTokenA.toString(),
                        tokenBAmountUsd = tokenBUsdAmount.toString(),
                        signature = result.signature
                    )

                    stateManager.onNewAction(SwapStateAction.CancelSwapLoading)
                    val transactionState = TransactionState.JupiterSwapSuccess
                    transactionManager.emitTransactionState(internalTransactionId, transactionState)

                    val pendingTransaction = buildPendingTransaction(result, currentState)
                    historyInteractor.addPendingTransaction(
                        txSignature = result.signature,
                        mintAddress = currentState.tokenA.mintAddress.base58Value,
                        transaction = pendingTransaction
                    )
                    view?.showDefaultSlider()
                }
                is JupiterSwapInteractor.JupiterSwapTokensResult.Failure -> {
                    val causeFailure = if (result.cause is JupiterSwapInteractor.LowSlippageRpcError) {
                        Timber.i("Swap failure: low slippage = ${currentState.slippage}")
                        TransactionStateSwapFailureReason.LowSlippage(currentState.slippage)
                    } else {
                        Timber.i("Swap failure: low slippage = unknown")
                        TransactionStateSwapFailureReason.Unknown(result.message.orEmpty())
                    }
                    val transactionState = TransactionState.JupiterSwapFailed(failure = causeFailure)
                    transactionManager.emitTransactionState(internalTransactionId, transactionState)
                    Timber.e(result, "Failed to swap tokens")
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
            is SwapState.TokenANotZero,
            is SwapState.LoadingRoutes,
            is SwapState.RoutesLoaded,
            is SwapState.LoadingTransaction, -> swapInteractor.getTokenAAmount(featureState)
            is SwapState.SwapException -> swapInteractor.getTokenAAmount(featureState.previousFeatureState)
            null -> null
        }
        if (allTokenAAmount != null) {
            currentFeatureState?.getTokensPair()?.first?.tokenSymbol?.let {
                analytics.logTokenAAllClicked(
                    tokenAName = it,
                    tokenAAmount = allTokenAAmount.toString()
                )
            }

            cancelRateJobs()
            onTokenAmountChange(allTokenAAmount.toPlainString())
        }
    }

    override fun onChangeTokenAClick() {
        if (isChangeTokenScreenAvailable(currentFeatureState)) {
            currentFeatureState?.getTokensPair()?.first?.let(analytics::logChangeTokenA)
            view?.openChangeTokenAScreen()
        }
    }

    override fun onChangeTokenBClick() {
        if (isChangeTokenScreenAvailable(currentFeatureState)) {
            currentFeatureState?.getTokensPair()?.second?.let(analytics::logChangeTokenB)
            view?.openChangeTokenBScreen()
        }
    }

    private fun isChangeTokenScreenAvailable(featureState: SwapState?): Boolean {
        return when (featureState) {
            null,
            SwapState.InitialLoading,
            -> false
            is SwapState.LoadingRoutes,
            is SwapState.LoadingTransaction,
            is SwapState.SwapLoaded,
            is SwapState.TokenANotZero,
            is SwapState.RoutesLoaded,
            is SwapState.TokenAZero, -> true
            is SwapState.SwapException -> isChangeTokenScreenAvailable(featureState.previousFeatureState)
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

    override fun onTryAgainClick() {
        retryAction()
    }

    override fun pauseStateManager() {
        stateManager.onNewAction(SwapStateAction.CancelSwapLoading)
    }

    override fun resumeStateManager() {
        stateManager.onNewAction(SwapStateAction.RefreshRoutes)
    }

    private fun handleNewFeatureState(state: SwapState) {
        // log analytics only on first TokenAZero
        if (shouldLogScreenOpened && state is SwapState.TokenAZero) {
            analytics.logStartScreen(
                openedFrom = swapOpenedFrom,
                initialTokenA = state.tokenA,
                initialTokenB = state.tokenB
            )
            shouldLogScreenOpened = false
        }

        cancelRateJobs()
        currentFeatureState = state
        when (state) {
            is SwapState.InitialLoading -> handleInitialLoading(state)
            is SwapState.TokenAZero -> handleTokenAZero(state)
            is SwapState.LoadingRoutes -> handleLoadingRoutes(state)
            is SwapState.RoutesLoaded -> handleRoutesLoaded(state)
            is SwapState.LoadingTransaction -> handleLoadingTransaction(state)
            is SwapState.SwapLoaded -> handleSwapLoaded(state)
            is SwapState.TokenANotZero -> handleTokenANotZero(state)
            is SwapState.SwapException.FeatureExceptionWrapper -> handleFeatureException(state)
            is SwapState.SwapException.OtherException -> handleOtherException(state)
        }
    }

    private fun handleOtherException(state: SwapState.SwapException.OtherException) {
        rateTickerManager.handleSwapException(state)
        mapWidgetStates(state)
        retryAction = {
            view?.hideFullScreenError()
            stateManager.onNewAction(state.lastSwapStateAction)
        }
        updateWidgets()
        view?.showFullScreenError()
    }

    private fun handleRateTickerChanges(state: SwapRateTickerState) {
        val ratioState: TextViewCellModel? = when (state) {
            is SwapRateTickerState.Shown -> rateTickerMapper.mapRateLoaded(state)
            is SwapRateTickerState.Loading -> rateTickerMapper.mapRateSkeleton(state)
            is SwapRateTickerState.Hidden -> null
        }
        view?.setRatioState(ratioState)
    }

    private fun handleFeatureException(state: SwapState.SwapException.FeatureExceptionWrapper) {
        Timber.i(state.featureException)

        rateTickerManager.handleSwapException(state)
        val (widgetAState, _) = mapWidgetStates(state.previousFeatureState)
        when (val featureException = state.featureException) {
            is SwapFeatureException.SameTokens -> {
                view?.setButtonState(buttonState = buttonMapper.mapSameToken())
            }
            is SwapFeatureException.SmallTokenAAmount -> {
                val tokenA = state.previousFeatureState.getTokensPair().first
                this.widgetAState = widgetMapper.mapErrorTokenAAmount(
                    tokenA = tokenA,
                    oldWidgetAState = widgetAState,
                    notValidAmount = featureException.notValidAmount
                )
                view?.setButtonState(buttonState = buttonMapper.mapSmallTokenAAmount())
            }
            is SwapFeatureException.RoutesNotFound -> {
                analytics.logSwapPairNotExists()
                view?.setButtonState(buttonState = buttonMapper.mapRouteNotFound())
            }
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
                analytics.logNotEnoughTokenA()
                view?.setButtonState(buttonState = buttonMapper.mapTokenAmountNotEnough(tokenA))
            }
            is SwapFeatureException.InsufficientSolBalance -> {
                val tokenA = state.previousFeatureState.getTokensPair().first

                this.widgetAState = widgetMapper.mapErrorTokenAAmount(
                    tokenA = tokenA,
                    oldWidgetAState = widgetAState,
                    notValidAmount = featureException.inputAmount
                )
                val solToken = featureException.userSolToken
                val remainingAmount = featureException.remainingAmount
                view?.setButtonState(buttonState = buttonMapper.mapInsufficientSolBalance(solToken, remainingAmount))
                view?.showSolErrorToast()
            }
        }
        updateWidgets()
    }

    private fun handleSwapLoaded(state: SwapState.SwapLoaded) {
        analytics.logChangeTokenBAmountChanged(state.tokenB, state.amountTokenB.toString())
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

    private fun checkPriceImpact() {
        val priceImpact = swapInteractor.getPriceImpact(currentFeatureState)
        when (val type = priceImpact?.toPriceImpactType()) {
            null, SwapPriceImpactView.NORMAL -> {
                view?.showPriceImpact(SwapPriceImpactView.NORMAL)
            }
            SwapPriceImpactView.YELLOW, SwapPriceImpactView.RED -> {
                if (type == SwapPriceImpactView.YELLOW) {
                    analytics.logPriceImpactLow(priceImpact)
                } else {
                    analytics.logPriceImpactHigh(priceImpact)
                }

                widgetBState = widgetMapper.mapPriceImpact(widgetBState, type)
                view?.showPriceImpact(type)
                if (needToScrollPriceImpact) {
                    view?.scrollToPriceImpact()
                    needToScrollPriceImpact = false
                }
            }
        }
    }

    private fun handleLoadingTransaction(state: SwapState.LoadingTransaction) {
        mapWidgetStates(state)
        updateWidgets()
        view?.setButtonState(buttonState = buttonMapper.mapLoading())
        checkPriceImpact()
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

    private fun handleTokenANotZero(state: SwapState.TokenANotZero) {
        mapWidgetStates(state)
        updateWidgets()
    }

    private fun handleRoutesLoaded(state: SwapState.RoutesLoaded) {
        mapWidgetStates(state)
        updateWidgets()
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
            is SwapState.RoutesLoaded ->
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
            is SwapState.TokenANotZero ->
                widgetMapper.mapTokenAAndSaveOldFiatAmount(
                    oldWidgetModel = widgetAState,
                    token = state.tokenA,
                    tokenAmount = state.amountTokenA
                ) to widgetMapper.mapTokenB(token = state.tokenB, tokenAmount = null)

            is SwapState.SwapException ->
                mapWidgetStates(state.previousFeatureState)
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
        widgetBState = widgetMapper.mapFiatAmount(
            state = state,
            oldWidgetModel = widgetBModel,
            tokenAmount = tokenAmount
        )
        view?.setSecondTokenWidgetState(state = widgetBState)
    }

    private fun updateWidgets() {
        view?.setFirstTokenWidgetState(state = widgetAState)
        if (needToShowKeyboard &&
            (widgetAState as? SwapWidgetModel.Content)?.amount is TextViewCellModel.Raw
        ) {
            view?.showKeyboard()
            needToShowKeyboard = false
        }
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

    private fun BigDecimal.toPriceImpactType(): SwapPriceImpactView {
        return when {
            isLessThan(onePercent) -> SwapPriceImpactView.NORMAL
            isLessThan(threePercent) -> SwapPriceImpactView.YELLOW
            else -> SwapPriceImpactView.RED
        }
    }

    private fun SwapState.getTokensPair(): Pair<SwapTokenModel?, SwapTokenModel?> {
        return swapInteractor.getSwapTokenPair(this)
    }

    private fun buildPendingTransaction(
        result: JupiterSwapInteractor.JupiterSwapTokensResult.Success,
        currentState: SwapState.SwapLoaded,
    ): RpcHistoryTransaction.Swap {
        return RpcHistoryTransaction.Swap(
            signature = result.signature,
            date = ZonedDateTime.now(),
            blockNumber = -1,
            status = HistoryTransactionStatus.PENDING,
            type = RpcHistoryTransactionType.SWAP,
            sourceSymbol = currentState.tokenA.tokenSymbol,
            sourceAddress = currentState.tokenA.mintAddress.toString(),
            fees = emptyList(),
            receiveAmount = RpcHistoryAmount(total = currentState.amountTokenA, totalInUsd = null),
            sentAmount = RpcHistoryAmount(total = currentState.amountTokenB, totalInUsd = null),
            sourceIconUrl = currentState.tokenA.iconUrl,
            destinationSymbol = currentState.tokenB.tokenSymbol,
            destinationIconUrl = currentState.tokenB.iconUrl,
            destinationAddress = currentState.tokenB.mintAddress.toString()
        )
    }
}
