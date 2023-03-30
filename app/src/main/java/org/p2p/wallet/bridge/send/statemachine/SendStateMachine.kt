package org.p2p.wallet.bridge.send.statemachine

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers

@OptIn(ExperimentalCoroutinesApi::class)
class SendStateMachine(
    private val handlers: Set<SendActionHandler>,
    private val dispatchers: CoroutineDispatchers,
) : CoroutineScope {

    companion object {
        const val SEND_FEE_EXPIRED_DURATION = 30_000L
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io
    private val state = MutableStateFlow<SendState>(SendState.Event.Empty)
    private val actions =
        MutableSharedFlow<Pair<SendFeatureAction, SendActionHandler>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    private var refreshFeeTimer: Job? = null

    fun observe(): StateFlow<SendState> = state

    init {
        actions
            .flatMapLatest {
                val (action, actionHandler) = it
                actionHandler.handle(action)
            }
            .flowOn(dispatchers.io)
            .catch { catchException(it) }
            .onEach { newState -> state.value = newState }
            .produceIn(this)
    }

    fun newAction(action: SendFeatureAction) {
        launch {
            Timber.tag("_______").d("Action = $action")
            val actionHandler = handlers.firstOrNull { it.canHandle(action) } ?: return@launch
            actions.emit(action to actionHandler)
        }
    }

    private fun catchException(throwable: Throwable) {
        when (throwable) {
            is CancellationException -> Timber.i(throwable)
            is Exception -> {}
        }
    }

    fun startRecurringAction(action: SendFeatureAction, withDelay: Long = SEND_FEE_EXPIRED_DURATION) {
        refreshFeeTimer?.cancel()
        refreshFeeTimer = launch {
            while (isActive) {
                newAction(action)
                delay(withDelay)
            }
        }
    }
}
