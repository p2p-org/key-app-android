package org.p2p.wallet.home.ui.main

import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.p2p.core.token.Token
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.user.interactor.UserInteractor

private val POLLING_ETH_DELAY = 30.toDuration(DurationUnit.SECONDS)

class UserTokensPolling(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor,
    private val dispatchers: CoroutineDispatchers,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + dispatchers.io

    private val isPollingEnabled: Boolean
        get() = appFeatureFlags.isPollingEnabled.featureValue

    private val solTokensFlow = MutableStateFlow<List<Token.Active>>(emptyList())
    private val ethTokensFlow = MutableStateFlow<List<Token.Eth>>(emptyList())
    private val isTokenRatesFetched = AtomicBoolean(false)
    private var refreshJob: Job? = null

    fun shareTokenPollFlowIn(scope: CoroutineScope): StateFlow<Pair<List<Token.Active>, List<Token.Eth>>> =
        solTokensFlow.combine(ethTokensFlow) { sol, eth ->
            sol to eth
        }.stateIn(scope, SharingStarted.WhileSubscribed(), Pair(emptyList(), emptyList()))

    suspend fun refresh() {
        solTokensFlow.emit(emptyList())
        ethTokensFlow.emit(emptyList())
        startPolling()
    }

    fun startPolling() {
        refreshJob?.cancel()
        refreshJob = launch {
            supervisorScope {
                if (isPollingEnabled) {
                    try {
                        while (true) {
                            delay(getDelayTimeInMillis(POLLING_ETH_DELAY.inWholeMilliseconds))
                            val newTokens = async { loadSolanaTokens() }
                            val ethBundles = async { ethereumInteractor.getListOfEthereumBundleStatuses() }
                            val ethTokens = async { ethereumInteractor.loadWalletTokens(ethBundles.await()) }
                            solTokensFlow.emit(newTokens.await())
                            ethTokensFlow.emit(ethTokens.await())
                            Timber.d("Successfully auto-updated loaded tokens")
                        }
                    } catch (e: CancellationException) {
                        Timber.i("Cancelled tokens remote update")
                    } catch (e: Throwable) {
                        Timber.e(e, "Failed polling tokens")
                    }
                } else {
                    Timber.d("Skipping tokens auto-update")
                }
            }
        }
    }

    private suspend fun loadSolanaTokens(): List<Token.Active> {
        return if (!isTokenRatesFetched.getAndSet(true)) {
            userInteractor.loadUserTokensAndUpdateLocal()
        } else {
            userInteractor.getUserTokens()
        }
    }

    private fun isForceFetchRequired(): Boolean = ethTokensFlow.value.isEmpty()

    private fun getDelayTimeInMillis(defaultValue: Long): Long = if (isForceFetchRequired()) 0L else defaultValue
}
