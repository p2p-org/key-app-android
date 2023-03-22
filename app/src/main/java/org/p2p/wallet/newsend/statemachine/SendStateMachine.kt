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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers

class SendStateMachine(
    private val handlers: Set<SendActionHandler>,
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

        val staticState = state.lastStaticState
        val actionHandler = handlers.firstOrNull { it.canHandle(action, staticState) } ?: return

        handleJob = actionHandler.handle(staticState, action)
            .flowOn(dispatchers.io)
            .catch { catchException(it) }
            .launchIn(this)
    }

    private fun catchException(throwable: Throwable) {
        when (throwable) {
            is CancellationException -> Timber.i(throwable)
            is SendFeatureException -> {
                Timber.e(throwable)
                if (throwable is SendFeatureException.FeeLoadingError) {
                    startFeeReloadTimer()
                }
                val lastStaticState = state.value.lastStaticState
                state.value = SendState.Exception.Feature(
                    lastStaticState,
                    SendFeatureException.FeeLoadingError,
                )
            }
            is Exception -> {
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
