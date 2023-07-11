package org.p2p.wallet.bridge

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.bridge.interactor.EthereumInteractor

private const val DELAY_IN_MILLISECONDS = 10_000L
private const val TAG = "EthereumTokensPollingService"

class EthereumTokensPollingService(
    private val ethereumInteractor: EthereumInteractor,
    appScope: AppScope
) : CoroutineScope {

    init {
        val job = appScope.coroutineContext.job
        job.invokeOnCompletion { pollingJob?.cancel() }
    }

    override val coroutineContext: CoroutineContext = appScope.coroutineContext
    private var pollingJob: Job? = null

    fun start() {
        pollingJob = launch {
            try {
                delay(DELAY_IN_MILLISECONDS)
                val claimTokens = ethereumInteractor.loadClaimTokens()
                val sendDetails = ethereumInteractor.loadSendTransactionDetails()
                ethereumInteractor.loadWalletTokens(claimTokens)
            } catch (e: Throwable) {
                Timber.tag(TAG).e(e, "Error while try to poll ethereum tokens")
            }
        }
    }
}
