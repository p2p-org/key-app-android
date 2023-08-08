package org.p2p.wallet.bridge.send.statemachine

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers

class SendStateMachine(
    private val handlers: Set<SendActionHandler>,
    private val dispatchers: CoroutineDispatchers,
) : CoroutineScope {

    companion object {
        const val SEND_FEE_EXPIRED_DURATION = 30_000L
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io
    private val state = MutableStateFlow<BridgeSendState>(BridgeSendState.Static.Empty)

    private var refreshFeeTimer: Job? = null
    private var actionHandleJob: Job? = null
    private var lastAction: SendFeatureAction = SendFeatureAction.InitFeature

    fun observe(): StateFlow<BridgeSendState> = state.asStateFlow()

    init {
        newAction(SendFeatureAction.InitFeature)
    }

    fun newAction(action: SendFeatureAction) {
        refreshFeeTimer?.cancel()
        actionHandleJob?.cancel()
        lastAction = action

        val staticState = state.lastStaticState
        val actionHandler = handlers.firstOrNull { it.canHandle(action, staticState) } ?: return
        actionHandleJob = actionHandler.handle(state.value, action)
            .flowOn(dispatchers.io)
            .catch { catchException(it, this) }
            .onEach { newState ->
                Timber.d("emit newState = $newState")
                startFeeTimerIfNeed(newState)
                state.value = newState
            }
            .launchIn(this)
    }

    private suspend fun catchException(throwable: Throwable, flowCollector: FlowCollector<BridgeSendState>) {
        when (throwable) {
            is CancellationException -> Timber.i(throwable)
            is SendFeatureException -> {
                Timber.e(throwable)
                if (throwable is SendFeatureException.FeeLoadingError) {
                    startFeeReloadTimer()
                }
                val lastStaticState = state.value.lastStaticState
                val wrappedState = BridgeSendState.Exception.Feature(
                    lastStaticState,
                    throwable,
                )
                flowCollector.emit(wrappedState)
            }
            else -> {
                val lastStaticState = state.value.lastStaticState
                val wrappedState = BridgeSendState.Exception.Other(
                    lastStaticState,
                    throwable,
                )
                flowCollector.emit(wrappedState)
            }
        }
    }

    private fun startFeeTimerIfNeed(newState: BridgeSendState) {
        val needStart = when (newState) {
            BridgeSendState.Static.Empty -> false
            is BridgeSendState.Loading.Fee -> {
                refreshFeeTimer?.cancel()
                false
            }
            is BridgeSendState.Exception,
            is BridgeSendState.Static.ReadyToSend,
            is BridgeSendState.Static.TokenNotZero,
            is BridgeSendState.Static.TokenZero -> true
        }
        if (needStart) startFeeReloadTimer()
    }

    private fun startFeeReloadTimer(delay: Long = SEND_FEE_EXPIRED_DURATION) {
        refreshFeeTimer?.cancel()
        refreshFeeTimer = launch {
            delay(delay)
            val lastState = state.value
            val lastAmount = if (lastState is BridgeSendState.Exception.Feature) {
                lastState.featureException.amount
            } else {
                lastState.lastStaticState.inputAmount
            }
            newAction(SendFeatureAction.RefreshFee(lastAmount))
        }
    }

    fun finishWork() {
        coroutineContext.cancelChildren()
    }
}
