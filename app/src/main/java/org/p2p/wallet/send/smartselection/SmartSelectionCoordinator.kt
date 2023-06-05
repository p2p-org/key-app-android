package org.p2p.wallet.send.smartselection

import timber.log.Timber
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.send.model.CalculationMode
import org.p2p.wallet.send.model.SendState
import org.p2p.wallet.send.model.smartselection.SmartSelectionState
import org.p2p.wallet.send.smartselection.handler.SimpleInitializationHandler
import org.p2p.wallet.send.smartselection.strategy.FeePayerSelectionStrategy

private const val TAG = "SmartSelectionCoordinator"

class SmartSelectionCoordinator(
    dispatchers: CoroutineDispatchers,
    private val feeCalculator: FeeCalculator,
    private val feePayerSelector: FeePayerSelector,
    private val calculationMode: CalculationMode
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io +
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()



//    private val internalState = MutableStateFlow<SmartSelectionState>(SmartSelectionState.Idle)

    fun onNewAction(newAction: UserSendAction) {
        when (newAction) {
            is UserSendAction.SimpleInitialization -> newAction.handleAction()
            is UserSendAction.AmountInitialization -> newAction.handleAction()
            is UserSendAction.AmountChanged -> TODO()
            is UserSendAction.SourceTokenChanged -> TODO()
            is UserSendAction.FeePayerChanged -> TODO()
            is UserSendAction.MaxInputEntered -> TODO()
            is UserSendAction.ToggleInputMode -> TODO()
        }
    }

    private fun UserSendAction.SimpleInitialization.handleAction() {
        launch {
            try {
                val handler = SimpleInitializationHandler(feePayerSelector, feeCalculator)
                handler.handleAction(this@handleAction)
            } catch (e: CancellationException) {
                Timber.tag(TAG).i("Empty Initialization handler cancelled")
            } catch (e: Throwable) {
                Timber.tag(TAG).i(e, "Empty Initialization handler failed")
                updateState(SmartSelectionState.Failed(e))
            }
        }
    }

    private fun UserSendAction.AmountInitialization.handleAction() {
        if (calculationMode.currencyMode is CurrencyMode.Fiat.Usd) {
            switchCurrencyMode()
        }
        val newTextValue = inputAmount.scaleShort().toPlainString()
        updateInputValue(newTextValue, forced = true)
        calculationMode.updateInputAmount(newTextValue)
    }

    private fun handleSplFee(feeInSpl: FeeCalculationState) {
        when (feeInSpl) {
            is FeeCalculationState.Success -> TODO()
//                launchFeePayerValidation(defaultToken, defaultToken, feeInSpl.fee)
            is FeeCalculationState.PoolsNotFound -> TODO()
        }
    }

    private fun handleAction(newTrigger: UserSendAction) {
        launch {
            try {
                triggerHandlers.forEach { it.handleTrigger(newTrigger) }
            } catch (e: CancellationException) {
                Timber.tag(TAG).i("Smart selection cancelled")
            } catch (e: Throwable) {
                Timber.tag(TAG).i(e, "Smart selection failed")
                updateState(SmartSelectionState.Failed(e))
            }

        }
    }

    private fun handleStrategies(strategies: List<FeePayerSelectionStrategy>) {
        val strategy = strategies.firstOrNull { it.isPayable() } ?: return
        strategy.execute()
    }

    private fun launchFeePayerValidation(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        fee: FeeRelayerFee
    ) {
        feePayerSelector.executeInitialSelection(sourceToken, feePayerToken, fee)
    }

    private fun updateState(newState: SmartSelectionState) {
        internalState.value = newState
    }
}
