package org.p2p.wallet.striga.offramp.interactor.polling

import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.utils.Constants
import org.p2p.wallet.infrastructure.coroutines.hasTestScheduler
import org.p2p.wallet.striga.exchange.repository.StrigaExchangeRepository
import org.p2p.wallet.striga.offramp.models.StrigaOffRampRateState
import org.p2p.wallet.utils.retryOnException

class StrigaOffRampExchangeRateNotifier(
    private val dispatchers: CoroutineDispatchers,
    private val strigaExchangeRepository: StrigaExchangeRepository,
) {

    private companion object {
        private val POLLING_INTERVAL: Duration = 60.seconds

        private var pollingJob: Job? = null
    }

    private val exchangeRateState = MutableStateFlow<StrigaOffRampRateState>(StrigaOffRampRateState.Loading)

    fun observeExchangeRateState(): StateFlow<StrigaOffRampRateState> = exchangeRateState.asStateFlow()

    fun start(scope: CoroutineScope) {
        if (isStarted()) {
            Timber.d("Start polling rates: skipped, already running")
            return
        }

        pollingJob = scope.launch(dispatchers.io) {
            while (isStarted()) {
                exchangeRateState.emit(StrigaOffRampRateState.Loading)

                try {
                    retryOnException(exceptionTypes = setOf(Throwable::class)) {
                        val result = strigaExchangeRepository.getExchangeRateForPair(
                            Constants.USDC_SYMBOL,
                            Constants.EUR_READABLE_SYMBOL,
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
        Timber.d("Start exchange rates polling: started")
    }

    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        Timber.d("Polling rates: stopped")
    }

    private fun isStarted(): Boolean = pollingJob?.isActive == true
}
