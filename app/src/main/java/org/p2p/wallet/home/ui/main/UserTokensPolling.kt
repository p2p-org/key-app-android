package org.p2p.wallet.home.ui.main

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    private var refreshJob: Job? = null

    fun shareTokenPollFlowIn(scope: CoroutineScope): StateFlow<Pair<List<Token.Active>?, List<Token.Eth>?>> =
        solTokensFlow.combine(ethTokensFlow) { sol, eth ->
            sol to eth
        }.stateIn(scope, SharingStarted.WhileSubscribed(), Pair(null, null))

    suspend fun refresh() {
        solTokensFlow.emit(null)
        ethTokensFlow.emit(null)
        initTokens()
    }

    fun initTokens() {
        launch {
            try {
                val tokensBefore = userInteractor.getUserTokens()
                val solTokens = async { fetchSolTokens() }
                val ethTokens = async { fetchEthereumTokens() }
                userInteractor.loadUserRates(solTokens.await())
                solTokensFlow.emit(userInteractor.getUserTokens())
                ethTokensFlow.emit(ethTokens.await())
                val tokensAfter = userInteractor.getUserTokens()
                startPolling()
            } catch (e: CancellationException) {
                Timber.i("Cancelled tokens remote update")
            } catch (e: Throwable) {
                Timber.e(e, "Failed polling tokens")
            }
        }
    }

    private fun startPolling() {
        refreshJob?.cancel()
        refreshJob = launch {
            if (isPollingEnabled) {
                try {
                    while (true) {
                        delay(POLLING_ETH_DELAY.inWholeMilliseconds)
                        val solTokens = async { fetchSolTokens() }
                        val ethTokens = async { fetchEthereumTokens() }
                        solTokensFlow.emit(solTokens.await())
                        ethTokensFlow.emit(ethTokens.await())
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

    private suspend fun fetchSolTokens() =
        userInteractor.loadUserTokensAndUpdateLocal(tokenKeyProvider.publicKey.toPublicKey())

    private suspend fun fetchEthereumTokens(): List<Token.Eth> {
        if (!ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            return emptyList()
        }
        val ethBundles = ethereumInteractor.getListOfEthereumBundleStatuses()
        return ethereumInteractor.loadWalletTokens(ethBundles)
    }
}
