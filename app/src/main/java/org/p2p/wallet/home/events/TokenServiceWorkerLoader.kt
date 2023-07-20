package org.p2p.wallet.home.events

import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.tokenservice.TokenServiceCoordinator

private val DELAY_IN_MINUTES = 5.minutes
private const val TAG = "TokenServiceWorkerLoader"

class TokenServiceWorkerLoader(
    private val appScope: AppScope,
    private val tokenServiceCoordinator: TokenServiceCoordinator
) : AppLoader, CoroutineScope by CoroutineScope(appScope.coroutineContext) {
    private var pollingJob: Job? = null

    override suspend fun onLoad() {
        start()
    }

    fun start() {
        pollingJob?.cancel()
        pollingJob = launch {
            while (isActive) {
                try {
                    Timber.tag(TAG).d("Update token rates")
                    delay(DELAY_IN_MINUTES)
                    tokenServiceCoordinator.refresh()
                } catch (e: Throwable) {
                    Timber.tag(TAG).e(e, "Error while try to refresh token rates")
                }
            }
        }
    }
}
