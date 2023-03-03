package org.p2p.wallet.swap.jupiter.statemanager

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.swap.JupiterSelectedSwapTokenStorageContract
import org.p2p.wallet.swap.jupiter.statemanager.handler.SwapStateHandler

private const val DELAY_IN_MILLIS = 20_000L

class SwapStateManager(
    private val handlers: Set<SwapStateHandler>,
    private val dispatchers: CoroutineDispatchers,
    private val selectedSwapTokenStorage: JupiterSelectedSwapTokenStorageContract
) : CoroutineScope {

    companion object {
        const val DEFAULT_ACTIVE_ROUTE_ORDINAL = 0
        const val DEFAULT_SLIPPAGE = 0.5
    }
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io
    private val state = MutableStateFlow<SwapState>(SwapState.InitialLoading)
    private var activeActionHandleJob: Job? = null
    private var refreshJob: Job? = null

    init {
        onNewAction(SwapStateAction.InitialLoading)
    }

    fun observe(): StateFlow<SwapState> = state

    suspend fun <T> getStateValue(getter: (state: SwapState) -> T): T {
        return getter.invoke(state.first())
    }

    fun onNewAction(action: SwapStateAction) {
        refreshJob?.cancel()
        activeActionHandleJob?.cancel()
        when (action) {
            is SwapStateAction.CancelSwapLoading -> return
            is SwapStateAction.TokenAChanged -> selectedSwapTokenStorage.savedTokenAMint = action.newTokenA.mintAddress
            is SwapStateAction.TokenBChanged -> selectedSwapTokenStorage.savedTokenBMint = action.newTokenB.mintAddress
            else -> Unit
        }

        activeActionHandleJob = launch {
            try {
                handleNewAction(action)
                val stateAfterHandle = state.value
                if (stateAfterHandle is SwapState.SwapLoaded) {
                    startRefreshJob()
                }
            } catch (cancelled: CancellationException) {
                Timber.i(cancelled)
            } catch (featureException: SwapFeatureException) {
                Timber.e(featureException)
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

    fun finishWork() {
        coroutineContext.cancelChildren()
    }
}
