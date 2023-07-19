package org.p2p.wallet.bridge

import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.bridge.interactor.EthereumInteractor

private const val DELAY_IN_MILLISECONDS = 10_000L
private const val TAG = "EthereumTokensPollingService"

class EthereumTokensPollingService(
    private val ethereumInteractor: EthereumInteractor,
    appScope: AppScope
) : CoroutineScope by CoroutineScope(appScope.coroutineContext) {

    private var pollingJob: Job? = null

    fun start() {
        pollingJob?.cancel()
        pollingJob = launch {
            while (isActive) {
                try {
                    delay(DELAY_IN_MILLISECONDS)
                    val claimTokens = ethereumInteractor.loadClaimTokens()
                    ethereumInteractor.loadSendTransactionDetails()
                    ethereumInteractor.loadWalletTokens(claimTokens)
                } catch (e: Throwable) {
                    Timber.tag(TAG).e(e, "Error while try to poll ethereum tokens")
                }
            }
        }
    }
}
