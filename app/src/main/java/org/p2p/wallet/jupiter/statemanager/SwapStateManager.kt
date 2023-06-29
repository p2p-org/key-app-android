package org.p2p.wallet.jupiter.statemanager

import timber.log.Timber
import java.math.BigDecimal
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.orZero
import org.p2p.wallet.common.feature_toggles.toggles.remote.SwapRoutesRefreshFeatureToggle
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.coroutines.hasTestScheduler
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.analytics.JupiterSwapMainScreenAnalytics
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.statemanager.handler.SwapStateHandler
import org.p2p.wallet.jupiter.statemanager.validator.SwapValidator
import org.p2p.wallet.jupiter.ui.main.SwapRateLoaderState
import org.p2p.wallet.jupiter.ui.main.SwapTokenRateLoader
import org.p2p.wallet.swap.model.Slippage
import org.p2p.core.crypto.Base58String
import org.p2p.token.service.interactor.TokenServiceInteractor

private const val TAG = "SwapStateManager"

class SwapStateManager(
    private val handlers: Set<SwapStateHandler>,
    private val dispatchers: CoroutineDispatchers,
    private val selectedSwapTokenStorage: JupiterSwapStorageContract,
    private val tokenServiceInteractor: TokenServiceInteractor,
    private val swapValidator: SwapValidator,
    private val analytics: JupiterSwapMainScreenAnalytics,
    private val homeLocalRepository: HomeLocalRepository,
    private val userTokensChangeHandler: SwapUserTokensChangeHandler,
    private val swapRoutesRefreshFeatureToggle: SwapRoutesRefreshFeatureToggle
) : CoroutineScope {

    companion object {
        const val DEFAULT_ACTIVE_ROUTE_ORDINAL = 0
        val DEFAULT_SLIPPAGE = Slippage.Medium
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io
    private val state = MutableStateFlow<SwapState>(SwapState.InitialLoading)
    private var activeActionHandleJob: Job? = null
    private var refreshJob: Job? = null
    private val tokenRatioCache = mutableMapOf<Base58String, SwapTokenRateLoader>()
    private var lastSwapStateAction: SwapStateAction = SwapStateAction.InitialLoading

    init {
        onNewAction(SwapStateAction.InitialLoading)
        observeUserTokens()
    }

    fun observe(): StateFlow<SwapState> = state

    suspend fun <T> getStateValue(getter: (state: SwapState) -> T): T {
        return getter.invoke(internalGetState(state.first()))
    }

    private fun internalGetState(state: SwapState): SwapState {
        return when (state) {
            is SwapState.SwapException -> internalGetState(state.previousFeatureState)
            else -> state
        }
    }

    fun onNewAction(action: SwapStateAction) {
        Timber.tag(TAG).i("new action triggered: $action")
        lastSwapStateAction = action
        refreshJob?.cancel()
        activeActionHandleJob?.cancel()
        when (action) {
            is SwapStateAction.CancelSwapLoading -> return
            is SwapStateAction.InitialLoading -> {
                state.value = SwapState.InitialLoading
            }
            is SwapStateAction.TokenAChanged -> {
                handleTokenAChange(action.newTokenA)
                return
            }
            is SwapStateAction.TokenBChanged -> {
                selectedSwapTokenStorage.savedTokenBMint = action.newTokenB.mintAddress
            }
            is SwapStateAction.SwitchTokens -> {
                handleSwitchTokensAndSaveTokenBAmount()
                return
            }
            else -> Unit
        }

        activeActionHandleJob = launch {
            try {
                handleNewAction(action)
                if (state.value is SwapState.SwapLoaded) startRefreshJob()
            } catch (exception: Throwable) {
                handleHandlerError(action, exception)
                if (exception is SwapFeatureException.RoutesNotFound) {
                    // retry to find routes
                    startRefreshJob()
                }
            }
        }
    }

    private fun handleHandlerError(action: SwapStateAction, exception: Throwable) {
        when (exception) {
            is CancellationException -> {
                Timber.tag(TAG).i(exception)
            }
            is SwapFeatureException -> {
                if (exception is SwapFeatureException.RoutesNotFound) {
                    Timber.tag(TAG).e(exception)
                } else {
                    Timber.tag(TAG).i(exception)
                }
                val actualStaticState = checkInNotLoadingOldNoErrorState(actualNoErrorState(), exception)
                state.value = SwapState.SwapException.FeatureExceptionWrapper(
                    previousFeatureState = actualStaticState,
                    featureException = exception,
                )
            }
            else -> {
                Timber.e(exception, "Failed to handle new action: $action")
                val actualStaticState = checkInNotLoadingOldNoErrorState(actualNoErrorState(), exception)
                state.value = SwapState.SwapException.OtherException(
                    previousFeatureState = actualStaticState,
                    exception = exception,
                    lastSwapStateAction = lastSwapStateAction,
                )
            }
        }
    }

    private suspend fun handleNewAction(action: SwapStateAction) {
        val currentState = actualNoErrorState()
        val actionHandler = handlers.firstOrNull { it.canHandle(currentState) } ?: return
        actionHandler.handleAction(state, currentState, action)
    }

    private fun startRefreshJob() {
        // this prevents route requests flooding in tests
        val singleShot = coroutineContext.hasTestScheduler

        refreshJob = launch {
            try {
                while (refreshJob?.isActive == true) {
                    delay(swapRoutesRefreshFeatureToggle.durationInMilliseconds)
                    val action = SwapStateAction.RefreshRoutes
                    lastSwapStateAction = action
                    handleNewAction(action)

                    if (singleShot) break
                }
            } catch (e: Throwable) {
                handleHandlerError(SwapStateAction.RefreshRoutes, e)
                if (isActive && e is SwapFeatureException.RoutesNotFound) {
                    startRefreshJob()
                }
            }
        }
    }

    private fun actualNoErrorState(): SwapState {
        var currentState = state.value
        if (currentState is SwapState.SwapException) {
            currentState = currentState.previousFeatureState
        }
        return currentState
    }

    private fun checkInNotLoadingOldNoErrorState(actualNoErrorState: SwapState, exception: Throwable): SwapState {
        return when (actualNoErrorState) {
            SwapState.InitialLoading,
            is SwapState.TokenANotZero,
            is SwapState.TokenAZero,
            is SwapState.RoutesLoaded,
            is SwapState.SwapLoaded -> actualNoErrorState
            is SwapState.LoadingRoutes ->
                SwapState.TokenANotZero(
                    tokenA = actualNoErrorState.tokenA,
                    tokenB = actualNoErrorState.tokenB,
                    amountTokenA = actualNoErrorState.amountTokenA,
                    slippage = actualNoErrorState.slippage,
                )
            is SwapState.LoadingTransaction ->
                SwapState.TokenANotZero(
                    tokenA = actualNoErrorState.tokenA,
                    tokenB = actualNoErrorState.tokenB,
                    amountTokenA = actualNoErrorState.amountTokenA,
                    slippage = actualNoErrorState.slippage,
                )
            is SwapState.SwapException.FeatureExceptionWrapper ->
                checkInNotLoadingOldNoErrorState(
                    actualNoErrorState.previousFeatureState,
                    actualNoErrorState.featureException
                )
            is SwapState.SwapException.OtherException ->
                checkInNotLoadingOldNoErrorState(
                    actualNoErrorState.previousFeatureState,
                    actualNoErrorState.exception
                )
        }
    }

    // change token A and set zero token A amount
    private fun handleTokenAChange(newTokenA: SwapTokenModel) {
        val oldTokenAZeroState = getOldTokenAZeroState(state.value) ?: return
        state.value = oldTokenAZeroState.copy(tokenA = newTokenA)
        if (!areValidTokens(newTokenA, oldTokenAZeroState.tokenB)) return
        selectedSwapTokenStorage.savedTokenAMint = newTokenA.mintAddress
    }

    // change switch tokens and try to set old token B amount to new token A amount else zero amount
    private fun handleSwitchTokensAndSaveTokenBAmount() {
        val oldTokenBAmount = getOldTokenBAmount(state.value).orZero()
        val oldTokenAZeroState = getOldTokenAZeroState(state.value) ?: return
        val newTokenA = oldTokenAZeroState.tokenB
        val newTokenB = oldTokenAZeroState.tokenA

        state.value = oldTokenAZeroState.copy(tokenA = newTokenA, tokenB = newTokenB)
        if (!areValidTokens(newTokenA, newTokenB)) return

        selectedSwapTokenStorage.savedTokenAMint = newTokenA.mintAddress
        selectedSwapTokenStorage.savedTokenBMint = newTokenB.mintAddress

        analytics.logTokensSwitchClicked(newTokenA = newTokenA, newTokenB = newTokenB)
        if (oldTokenBAmount.isNotZero()) onNewAction(SwapStateAction.TokenAAmountChanged(oldTokenBAmount))
    }

    private fun getOldTokenAZeroState(
        state: SwapState,
    ): SwapState.TokenAZero? {
        fun mapState(
            oldTokenA: SwapTokenModel,
            oldTokenB: SwapTokenModel,
            slippage: Slippage
        ): SwapState.TokenAZero = SwapState.TokenAZero(oldTokenA, oldTokenB, slippage)
        return when (state) {
            SwapState.InitialLoading -> null
            is SwapState.TokenAZero -> state
            is SwapState.LoadingRoutes -> with(state) { mapState(tokenA, tokenB, slippage) }
            is SwapState.LoadingTransaction -> with(state) { mapState(tokenA, tokenB, slippage) }
            is SwapState.SwapLoaded -> with(state) { mapState(tokenA, tokenB, slippage) }
            is SwapState.SwapException -> getOldTokenAZeroState(state.previousFeatureState)
            is SwapState.TokenANotZero -> with(state) { mapState(tokenA, tokenB, slippage) }
            is SwapState.RoutesLoaded -> with(state) { mapState(tokenA, tokenB, slippage) }
        }
    }

    private fun getOldTokenBAmount(state: SwapState): BigDecimal? {
        return when (state) {
            SwapState.InitialLoading,
            is SwapState.TokenAZero,
            is SwapState.TokenANotZero,
            is SwapState.LoadingRoutes -> null
            is SwapState.RoutesLoaded -> state.amountTokenB
            is SwapState.LoadingTransaction -> state.amountTokenB
            is SwapState.SwapLoaded -> state.amountTokenB
            is SwapState.SwapException -> getOldTokenBAmount(state.previousFeatureState)
        }
    }

    private fun areValidTokens(tokenA: SwapTokenModel, tokenB: SwapTokenModel): Boolean {
        return try {
            swapValidator.validateIsSameTokens(tokenA = tokenA, tokenB = tokenB)
            true
        } catch (featureException: SwapFeatureException.SameTokens) {
            state.value = SwapState.SwapException.FeatureExceptionWrapper(
                previousFeatureState = state.value,
                featureException = featureException,
            )
            false
        }
    }

    fun finishWork() {
        coroutineContext.cancelChildren()
    }

    fun getTokenRate(token: SwapTokenModel): Flow<SwapRateLoaderState> {
        return tokenRatioCache.getOrPut(token.mintAddress) {
            SwapTokenRateLoader(tokenServiceInteractor)
        }.getRate(token)
    }

    suspend fun getTokenRateLoadedOrNull(token: SwapTokenModel): SwapRateLoaderState.Loaded? {
        return getTokenRate(token)
            .filterIsInstance<SwapRateLoaderState.Loaded>()
            .firstOrNull()
    }

    private fun observeUserTokens() {
        homeLocalRepository.getTokensFlow()
            .mapLatest { userTokens ->
                userTokensChangeHandler.handleUserTokensChange(state.value, userTokens)
            }
            .onEach { newState -> state.value = newState }
            .flowOn(dispatchers.io)
            .launchIn(this)
    }
}
