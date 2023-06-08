package org.p2p.wallet.newsend.smartselection

import timber.log.Timber
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason
import org.p2p.wallet.newsend.model.smartselection.SmartSelectionState
import org.p2p.wallet.newsend.smartselection.handler.TriggerHandler
import org.p2p.wallet.newsend.smartselection.strategy.StrategyExecutor

private const val TAG = "SmartSelectionCoordinator"

class SmartSelectionCoordinator(
    private val strategyExecutor: StrategyExecutor,
    private val triggerHandlers: List<TriggerHandler>,
    dispatchers: CoroutineDispatchers
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io +
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val feePayerState = MutableStateFlow<FeePayerState>(FeePayerState.Idle)

    private var recentTrigger: SmartSelectionTrigger? = null

    private lateinit var feePayerToken: Token.Active

    private val internalSmartSelectionState = MutableStateFlow<SmartSelectionState>(SmartSelectionState.Cancelled)

    private var triggerJob: Job? = null

    init {
        launch {
            internalSmartSelectionState
                .stateIn(this)
                .collectLatest { handleSmartSelectionState(it) }
        }
    }

    fun getFeePayerStateFlow(): StateFlow<FeePayerState> = feePayerState.asStateFlow()

    fun isTransactionFree(): Boolean = feePayerState.value.isTransactionFree()

    fun getFeeData(): Pair<Token.Active, FeeRelayerFee>? {
        return when (val currentState = feePayerState.value) {
            is FeePayerState.CalculationSuccess -> currentState.feePayerToken to currentState.fee
            is FeePayerState.ReduceAmount -> currentState.feePayerToken to currentState.fee
            else -> null
        }
    }

    fun setInitialFeePayer(initialFeePayerToken: Token.Active) {
        this.feePayerToken = initialFeePayerToken
    }

    fun onNewTrigger(newTrigger: SmartSelectionTrigger) {
        if (recentTrigger is SmartSelectionTrigger.FeePayerManuallyChanged) {
            // just calculate fees with current fee payer token without smart selection
            return
        }

        recentTrigger = newTrigger

        triggerJob?.cancel()

        launch {
            try {
                triggerHandlers.forEach { it.handleTrigger(internalSmartSelectionState, newTrigger, feePayerToken) }
            } catch (e: CancellationException) {
                Timber.tag(TAG).i("Empty Initialization handler cancelled")
            } catch (e: Throwable) {
                Timber.tag(TAG).i(e, "Empty Initialization handler failed")
                internalSmartSelectionState.value = SmartSelectionState.Failed(e)
            }
        }.also { triggerJob = it }
    }

    private fun handleSmartSelectionState(state: SmartSelectionState) {
        when (state) {
            is SmartSelectionState.ReadyForSmartSelection -> {
                strategyExecutor.setStrategies(state.strategies)
                val newFeePayerState = strategyExecutor.execute()
                if (newFeePayerState is FeePayerState.CalculationSuccess) {
                    feePayerToken = newFeePayerState.feePayerToken
                }

                updateState(newFeePayerState)
            }
            is SmartSelectionState.SolanaFeeOnly -> {
                // TODO
            }
            is SmartSelectionState.NoFees -> {
                val noFeesState = FeePayerState.FreeTransaction(
                    sourceToken = state.sourceToken,
                    initialAmount = state.initialAmount
                )
                updateState(noFeesState)
            }
            is SmartSelectionState.Failed -> {
                val reason = FeePayerFailureReason.CalculationError(state.e)
                val failureState = FeePayerState.Failure(reason)
                updateState(failureState)
            }
            is SmartSelectionState.Cancelled -> Unit
        }
    }

    fun release() {
        coroutineContext.cancelChildren()
    }

    private fun updateState(newState: FeePayerState) {
        feePayerState.value = newState
    }
}
