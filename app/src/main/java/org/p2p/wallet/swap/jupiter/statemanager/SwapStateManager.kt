package org.p2p.wallet.swap.jupiter.statemanager

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.p2p.core.utils.isNotZero
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.statemanager.handler.SwapStateHandler
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.ui.jupiter.main.SwapRateLoaderState
import org.p2p.wallet.swap.ui.jupiter.main.SwapTokenRateLoader
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.utils.Base58String

private const val DELAY_IN_MILLIS = 20_000L

private const val TAG = "SwapStateManager"

class SwapStateManager(
    private val handlers: Set<SwapStateHandler>,
    private val dispatchers: CoroutineDispatchers,
    private val selectedSwapTokenStorage: JupiterSwapStorageContract,
    private val tokenPricesRepository: TokenPricesRemoteRepository,
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

    init {
        onNewAction(SwapStateAction.InitialLoading)
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
        refreshJob?.cancel()
        activeActionHandleJob?.cancel()
        when (action) {
            is SwapStateAction.CancelSwapLoading -> return
            is SwapStateAction.TokenAChanged -> {
                selectedSwapTokenStorage.savedTokenAMint = action.newTokenA.mintAddress
                if (handleTokenAChange(action.newTokenA)) return
            }
            is SwapStateAction.TokenBChanged -> {
                selectedSwapTokenStorage.savedTokenBMint = action.newTokenB.mintAddress
            }
            is SwapStateAction.SwitchTokens -> {
                if (handleSwitchTokensAndSaveTokenBAmount()) return
            }
            else -> Unit
        }

        activeActionHandleJob = launch {
            try {
                handleNewAction(action)
                if (state.value is SwapState.SwapLoaded) startRefreshJob()
            } catch (cancelled: CancellationException) {
                Timber.tag(TAG).i(cancelled)
            } catch (featureException: SwapFeatureException) {
                if (featureException is SwapFeatureException.RoutesNotFound) {
                    Timber.tag(TAG).e(featureException)
                } else {
                    Timber.tag(TAG).i(featureException)
                }
                state.value = SwapState.SwapException.FeatureExceptionWrapper(
                    previousFeatureState = actualNoErrorState(),
                    featureException = featureException,
                )
            } catch (exception: Throwable) {
                Timber.e(exception)
                state.value = SwapState.SwapException.OtherException(
                    previousFeatureState = actualNoErrorState(),
                    exception = exception,
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
        refreshJob = launch {
            try {
                while (refreshJob?.isActive == true) {
                    delay(DELAY_IN_MILLIS)
                    handleNewAction(SwapStateAction.RefreshRoutes)
                }
            } catch (e: Throwable) {
                Timber.e(e)
                // todo ignore?
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

    private fun handleTokenAChange(newTokenA: SwapTokenModel): Boolean {
        val oldTokenAZeroState = getOldTokenAZeroState(state.value) ?: return false
        state.value = oldTokenAZeroState.copy(tokenA = newTokenA)
        return true
    }

    private fun handleSwitchTokensAndSaveTokenBAmount(): Boolean {
        val oldTokenBAmount = getOldTokenBAmount(state.value) ?: BigDecimal.ZERO
        val oldTokenAZeroState = getOldTokenAZeroState(state.value) ?: return false
        val oldTokenA = oldTokenAZeroState.tokenA
        val oldTokenB = oldTokenAZeroState.tokenB
        state.value = oldTokenAZeroState.copy(tokenA = oldTokenB, tokenB = oldTokenA)
        if (oldTokenBAmount.isNotZero()) onNewAction(SwapStateAction.TokenAAmountChanged(oldTokenBAmount))
        return true
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
        }
    }

    private fun getOldTokenBAmount(state: SwapState): BigDecimal? {
        return when (state) {
            SwapState.InitialLoading,
            is SwapState.TokenAZero -> null
            is SwapState.LoadingRoutes -> null
            is SwapState.LoadingTransaction -> state.amountTokenB
            is SwapState.SwapLoaded -> state.amountTokenB
            is SwapState.SwapException -> getOldTokenBAmount(state.previousFeatureState)
        }
    }

    fun finishWork() {
        coroutineContext.cancelChildren()
    }

    fun getTokenRate(token: SwapTokenModel): Flow<SwapRateLoaderState> {
        return tokenRatioCache.getOrPut(token.mintAddress) {
            SwapTokenRateLoader(tokenPricesRepository)
        }.getRate(token)
    }
}
