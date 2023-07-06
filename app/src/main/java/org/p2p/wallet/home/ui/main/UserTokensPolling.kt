package org.p2p.wallet.home.ui.main

import timber.log.Timber
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.striga.wallet.interactor.StrigaClaimInteractor
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toPublicKey

private val POLLING_ETH_DELAY = 30.toDuration(DurationUnit.SECONDS)
private const val TAG = "UserTokensPolling"

class UserTokensPolling(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val strigaClaimInteractor: StrigaClaimInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val appScope: AppScope
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = appScope.coroutineContext

    private val isPollingEnabled: Boolean
        get() = appFeatureFlags.isPollingEnabled.featureValue

    private val isRefreshingFlow = MutableStateFlow(false)
    private var homeScreenState = UserTokensPollState()

    private var refreshJob: Job? = null

    fun shareTokenPollFlowIn(scope: CoroutineScope): StateFlow<UserTokensPollState?> =
        userInteractor.getUserTokensFlow()
            .combine(getEthereumTokensFlow()) { solTokens, ethTokens ->
                val claimableTokens = strigaClaimInteractor.getClaimableTokens()
                    .successOrNull()
                    .orEmpty()

                homeScreenState = if (homeScreenState.solTokens.isNotEmpty() && solTokens.isEmpty()) {
                    homeScreenState.copy(ethTokens = ethTokens)
                } else {
                    homeScreenState.copy(solTokens = solTokens, ethTokens = ethTokens)
                }
                homeScreenState.copy(claimableTokens = claimableTokens)
            }.combine(isRefreshingFlow) { currentState, refreshing ->
                this.homeScreenState = currentState.copy(isRefreshing = refreshing)
                homeScreenState
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(), null)

    fun refreshTokens() {
        launch {
            try {
                isRefreshingFlow.emit(true)
                val userTokens = fetchSolTokens()
                userInteractor.loadUserRatesIfEmpty(userTokens)
                startPolling()
            } catch (e: CancellationException) {
                Timber.i("Cancelled tokens remote update")
            } catch (e: UnknownHostException) {
                Timber.i(e, "Failed polling tokens: no internet connection")
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
                    while (isActive) {
                        delay(POLLING_ETH_DELAY.inWholeMilliseconds)
                        fetchSolTokens()
                    }
                } catch (e: CancellationException) {
                    Timber.i("Cancelled tokens remote update")
                } catch (e: UnknownHostException) {
                    Timber.i(e, "Failed polling tokens: no internet connection")
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

    private fun getEthereumTokensFlow(): Flow<List<Token.Eth>> {
        return ethereumInteractor.getTokensFlow()
    }
}

data class UserTokensPollState(
    val solTokens: List<Token.Active> = emptyList(),
    val ethTokens: List<Token.Eth> = emptyList(),
    val claimableTokens: List<StrigaClaimableToken> = emptyList(),
    val isRefreshing: Boolean = false
)
