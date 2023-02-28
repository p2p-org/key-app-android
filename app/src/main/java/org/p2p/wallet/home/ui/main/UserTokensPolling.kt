package org.p2p.wallet.home.ui.main

import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.core.token.Token
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.common.di.AppScope

private val POLLING_DELAY = 10.toDuration(DurationUnit.SECONDS)

class UserTokensPolling(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val appScope: AppScope
) {

    private val isPollingEnabled: Boolean
        get() = appFeatureFlags.isPollingEnabled.featureValue

    fun startPolling(onTokensLoaded: (List<Token.Active>) -> Unit) {
        appScope.launch {
            try {
                while (true) {
                    delay(POLLING_DELAY.inWholeMilliseconds)
                    if (isPollingEnabled) {
                        val newTokens = userInteractor.loadUserTokensAndUpdateLocal(fetchPrices = false)
                        onTokensLoaded.invoke(newTokens)
                        Timber.d("Successfully auto-updated loaded tokens")
                    } else {
                        Timber.d("Skipping tokens auto-update")
                    }
                }
            } catch (e: CancellationException) {
                Timber.i("Cancelled tokens remote update")
            } catch (e: Throwable) {
                Timber.e(e, "Failed polling tokens")
            }
        }
    }
}
