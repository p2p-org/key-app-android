package org.p2p.wallet.striga.offramp.interactor

import timber.log.Timber
import java.math.BigDecimal
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.utils.Constants
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.wallet.infrastructure.coroutines.hasTestScheduler
import org.p2p.wallet.striga.exchange.models.StrigaExchangeRate
import org.p2p.wallet.striga.exchange.repository.StrigaExchangeRepository
import org.p2p.wallet.striga.offramp.models.StrigaOffRampButtonState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampRateState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampTokenType
import org.p2p.wallet.utils.divideSafe
import org.p2p.wallet.utils.retryOnException

class StrigaOffRampInteractor(
    private val dispatchers: CoroutineDispatchers,
    private val strigaExchangeRepository: StrigaExchangeRepository,
) {

    companion object {
        private val POLLING_INTERVAL: Duration = 1.minutes

        private val MIN_EUR_AMOUNT = BigDecimal("10")
        private val MAX_EUR_AMOUNT = BigDecimal("15000")
    }

    private val exchangeRateState = MutableStateFlow<StrigaOffRampRateState>(StrigaOffRampRateState.Loading)

    private var pollingJob: Job? = null

    fun observeExchangeRateState(): Flow<StrigaOffRampRateState> = exchangeRateState.asStateFlow()

    fun isPollingRunning(): Boolean = pollingJob?.isActive == true

    fun startPolling(scope: CoroutineScope) {
        if (isPollingRunning()) {
            Timber.d("Start polling rates: skipped, already running")
            return
        }
        Timber.d("Start polling rates: in progress...")
        pollingJob = scope.launch(dispatchers.io) {
            while (isPollingRunning()) {
                exchangeRateState.emit(StrigaOffRampRateState.Loading)

                try {
                    retryOnException(exceptionTypes = setOf(Throwable::class)) {
                        val result = strigaExchangeRepository.getExchangeRateForPair(
                            Constants.USDC_SYMBOL,
                            Constants.EUR_SYMBOL,
                        ).unwrap()

                        exchangeRateState.emit(
                            StrigaOffRampRateState.Success(result)
                        )
                    }
                } catch (e: CancellationException) {
                    Timber.d("Polling was cancelled")
                } catch (e: Throwable) {
                    exchangeRateState.emit(StrigaOffRampRateState.Failure(e))
                }

                if (coroutineContext.hasTestScheduler) {
                    // don't poll in tests
                    break
                }

                delay(POLLING_INTERVAL)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun calculateAmountByRate(
        tokenType: StrigaOffRampTokenType,
        rate: StrigaExchangeRate?,
        amount: BigDecimal
    ): BigDecimal {
        return when (tokenType) {
            StrigaOffRampTokenType.TokenA -> {
                amount.divideSafe(rate?.buyRate.orZero())
            }
            StrigaOffRampTokenType.TokenB -> {
                amount * rate?.buyRate.orZero()
            }
        }
    }

    fun validateAmount(
        amountA: BigDecimal,
        amountB: BigDecimal,
        amountAvailable: BigDecimal
    ): StrigaOffRampButtonState {
        return when {
            amountA.isZero() -> StrigaOffRampButtonState.EnterAmount
            amountA > amountAvailable -> StrigaOffRampButtonState.ErrorInsufficientFunds
            amountB < MIN_EUR_AMOUNT -> StrigaOffRampButtonState.ErrorMinLimit
            amountB > MAX_EUR_AMOUNT -> StrigaOffRampButtonState.ErrorMaxLimit
            else -> StrigaOffRampButtonState.Enabled
        }
    }
}
