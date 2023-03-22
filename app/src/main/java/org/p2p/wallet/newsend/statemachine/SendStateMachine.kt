package org.p2p.wallet.newsend.statemachine

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers

class SendStateMachine(
    private val handlers: List<SendActionHandler>,
    private val dispatchers: CoroutineDispatchers,
) : CoroutineScope {

    companion object {
        const val SEND_FEE_EXPIRED_DURATION = 30_000L
    }

    private val state = MutableStateFlow<SendState>(SendState.Static.Empty)
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io

    private var handleJob: Job? = null
    private var refreshFeeTimer: Job? = null
    private var lastAction: SendFeatureAction = SendFeatureAction.InitFeature

    fun observe(): StateFlow<SendState> = state.asStateFlow()

    init {
        newAction(SendFeatureAction.InitFeature)
    }

    fun newAction(action: SendFeatureAction) {
        handleJob?.cancel()
        refreshFeeTimer?.cancel()
        lastAction = action

        handleJob = launch {
            try {
                val staticState = state.lastStaticState
                val actionHandler = handlers.firstOrNull { it.canHandle(action, staticState) } ?: return@launch
                actionHandler.handle(state, staticState, action)
            } catch (e: CancellationException) {
                Timber.i(e)
            } catch (e: SendFeatureException) {
                if (e is SendFeatureException.FeeLoadingError) {
                    startFeeReloadTimer()
                }
                val lastStaticState = state.value.lastStaticState
                state.value = SendState.Exception.Feature(
                    lastStaticState,
                    SendFeatureException.FeeLoadingError,
                )
            } catch (e: Exception) {
                val lastStaticState = state.value.lastStaticState
                state.value = SendState.Exception.Other(
                    lastStaticState,
                    SendFeatureException.FeeLoadingError,
                )
            }
        }
    }

    private fun startFeeReloadTimer() {
        refreshFeeTimer = launch {
            delay(SEND_FEE_EXPIRED_DURATION)
            newAction(SendFeatureAction.RefreshFee)
        }
    }

    fun finishWork() {
        coroutineContext.cancelChildren()
    }
}
