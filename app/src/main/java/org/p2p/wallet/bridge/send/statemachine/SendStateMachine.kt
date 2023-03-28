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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers

class SendStateMachine(
    private val handlers: Set<SendActionHandler>,
    private val dispatchers: CoroutineDispatchers,
) : CoroutineScope {

    companion object {
        const val SEND_FEE_EXPIRED_DURATION = 30_000L
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io
    private val state = MutableStateFlow<SendState>(SendState.Event.Empty)
    private val actions = MutableSharedFlow<Pair<SendFeatureAction, SendActionHandler>>()

    private var refreshFeeTimer: Job? = null

    fun observe(): StateFlow<SendState> = state.asStateFlow()

    init {
        actions
            .flatMapLatest {
                val (action, actionHandler) = it
                actionHandler.handle(action)
            }
            .flowOn(dispatchers.io)
            .catch { catchException(it, this) }
            .onEach { newState -> state.value = newState }
            .launchIn(this)
    }

    fun newAction(action: SendFeatureAction) {
        refreshFeeTimer?.cancel()

        val actionHandler = handlers.firstOrNull { it.canHandle(action) } ?: return

        actions.tryEmit(action to actionHandler)
    }

    private suspend fun catchException(throwable: Throwable, flowCollector: FlowCollector<SendState>) {
        when (throwable) {
            is CancellationException -> Timber.i(throwable)
            is SendFeatureException -> {
                Timber.e(throwable)
                if (throwable is SendFeatureException.FeeLoadingError) {
                    startFeeReloadTimer()
                }
                val wrappedState = SendState.Exception.Feature(
                    SendFeatureException.FeeLoadingError
                )
                flowCollector.emit(wrappedState)
            }
            is Exception -> {
                val wrappedState = SendState.Exception.Other(
                    SendFeatureException.FeeLoadingError
                )
                flowCollector.emit(wrappedState)
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
