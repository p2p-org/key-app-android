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
import kotlinx.coroutines.joinAll
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

    private val ethTokensFlow = MutableStateFlow<List<Token.Eth>>(emptyList())
    private val isRefreshingFlow = MutableStateFlow<Boolean>(false)
    private var homeScreenState = UserTokensPollState()

    private var refreshJob: Job? = null

    fun shareTokenPollFlowIn(scope: CoroutineScope): StateFlow<UserTokensPollState?> =
        userInteractor.getUserTokensFlow()
            .combine(ethTokensFlow) { sol, eth ->
                homeScreenState = if (homeScreenState.solTokens.isNotEmpty() && sol.isEmpty()) {
                    homeScreenState.copy(ethTokens = eth)
                } else {
                    homeScreenState.copy(solTokens = sol, ethTokens = eth)
                }
                homeScreenState
            }.combine(isRefreshingFlow) { currentState, refreshing ->
                this.homeScreenState = currentState.copy(isRefreshing = refreshing)
                homeScreenState
            }.stateIn(scope, SharingStarted.WhileSubscribed(), null)

    suspend fun refresh() {
        ethTokensFlow.emit(emptyList())
        initTokens()
    }

    fun initTokens() {
        launch {
            try {
                isRefreshingFlow.emit(true)
                val ethTokens = fetchEthereumTokens()
                ethTokensFlow.emit(ethTokens)
                startPolling()
            } catch (e: CancellationException) {
                Timber.i("Cancelled tokens remote update")
            } catch (e: Throwable) {
                Timber.e(e, "Failed polling tokens")
            } finally {
                isRefreshingFlow.emit(false)
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
                        joinAll(
                            async { fetchSolTokens() },
                            async {
                                val ethTokens = fetchEthereumTokens()
                                ethTokensFlow.emit(ethTokens)
                            }
                        )
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

    private suspend fun fetchSolTokens(): List<Token.Active> =
        userInteractor.loadUserTokensAndUpdateLocal(tokenKeyProvider.publicKey.toPublicKey())

    private suspend fun fetchEthereumTokens(): List<Token.Eth> {
        if (!ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            return emptyList()
        }
        val ethBundles = ethereumInteractor.getListOfEthereumBundleStatuses()
        return ethereumInteractor.loadWalletTokens(ethBundles)
    }
}

data class UserTokensPollState(
    val solTokens: List<Token.Active> = emptyList(),
    val ethTokens: List<Token.Eth> = emptyList(),
    val isRefreshing: Boolean = false
)
