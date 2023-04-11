package org.p2p.wallet.home.ui.main

import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toPublicKey

private val POLLING_ETH_DELAY = 30.toDuration(DurationUnit.SECONDS)
private val TAG = "UserTokensPolling"

class UserTokensPolling(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor,
    private val dispatchers: CoroutineDispatchers,
    private val tokenKeyProvider: TokenKeyProvider
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + dispatchers.io

    private val isPollingEnabled: Boolean
        get() = appFeatureFlags.isPollingEnabled.featureValue

    private val solTokensFlow = MutableStateFlow<List<Token.Active>?>(null)
    private val ethTokensFlow = MutableStateFlow<List<Token.Eth>?>(null)

    private val isTokensRateFetched = AtomicBoolean(false)
    private var refreshJob: Job? = null

    fun shareTokenPollFlowIn(scope: CoroutineScope): StateFlow<Pair<List<Token.Active>?, List<Token.Eth>?>> =
        solTokensFlow.combine(ethTokensFlow) { sol, eth ->
            sol to eth
        }.stateIn(scope, SharingStarted.WhileSubscribed(), Pair(emptyList(), emptyList()))

    suspend fun refresh() {
        isTokensRateFetched.set(false)
        solTokensFlow.emit(null)
        ethTokensFlow.emit(null)
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
                            fetchSolanaTokens()
                            fetchEthereumTokens()
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

    private suspend fun fetchSolanaTokens() {
        if (!isTokensRateFetched.get()) {
            loadSolTokensRate()
        }
        solTokensFlow.emit(userInteractor.loadUserTokensAndUpdateLocal(tokenKeyProvider.publicKey.toPublicKey()))
    }

    private suspend fun getSolTokens(): List<Token.Active> {
        return userInteractor.getUserTokens().ifEmpty {
            userInteractor.loadUserTokensAndUpdateLocal(tokenKeyProvider.publicKey.toPublicKey())
        }
    }

    private suspend fun fetchEthereumTokens() = withContext(dispatchers.io) {
        if (!ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            ethTokensFlow.emit(emptyList())
            return@withContext
        }
        val ethBundles = ethereumInteractor.getListOfEthereumBundleStatuses()
        val ethTokens = ethereumInteractor.loadWalletTokens(ethBundles)
        ethTokensFlow.emit(ethTokens)
    }

    private fun loadSolTokensRate() {
        launch {
            try {
                userInteractor.loadUserRates(getSolTokens())
                isTokensRateFetched.set(true)
                solTokensFlow.emit(getSolTokens())
            } catch (e: CancellationException) {
                Timber.tag(TAG).d("Loading tokens rate was cancelled")
            } catch (e: Throwable) {
                Timber.tag(TAG).e(e, "Loading tokens rate finished with error: $e")
            }
        }
    }

    private fun isForceFetchRequired(): Boolean =
        ethTokensFlow.value.isNullOrEmpty() && solTokensFlow.value.isNullOrEmpty()

    private fun getDelayTimeInMillis(defaultValue: Long): Long = if (isForceFetchRequired()) 0L else defaultValue
}
