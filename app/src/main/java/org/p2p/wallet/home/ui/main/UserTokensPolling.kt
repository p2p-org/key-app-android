package org.p2p.wallet.home.ui.main

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.repository.EthereumTokensProvider
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.user.interactor.UserInteractor

private val POLLING_DELAY = 10.toDuration(DurationUnit.SECONDS)

class UserTokensPolling(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor,
    private val ethTokenProvider: EthereumTokensProvider,
    private val dispatchers: CoroutineDispatchers,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + dispatchers.io

    private val isPollingEnabled: Boolean
        get() = appFeatureFlags.isPollingEnabled.featureValue

    private val solTokensFlow = MutableStateFlow<List<Token.Active>>(emptyList())

    fun shareTokenPollFlowIn(scope: CoroutineScope): StateFlow<Pair<List<Token.Active>, List<Token.Eth>>> =
        solTokensFlow.combine(ethTokenProvider.erc20Tokens) { sol, eth ->
            sol to eth
        }.stateIn(scope, SharingStarted.WhileSubscribed(), Pair(emptyList(), emptyList()))

    fun startPolling() {
        startPollingSolana()
        startPollingEthereum()
    }

    suspend fun refresh() {
        solTokensFlow.emit(emptyList())
    }

    private fun startPollingSolana() {
        launch {
            if (isPollingEnabled) {
                try {
                    while (true) {
                        delay(POLLING_DELAY.inWholeMilliseconds)
                        val newTokens = userInteractor.loadUserTokensAndUpdateLocal()
                        solTokensFlow.emit(newTokens)
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

    private fun startPollingEthereum() {
        launch {
            if (ethAddressEnabledFeatureToggle.isFeatureEnabled) {
                try {
                    if (ethAddressEnabledFeatureToggle.isFeatureEnabled) {
                        val bundles = ethereumInteractor.getListOfEthereumBundleStatuses()
                        ethTokenProvider.launch(claimingTokens = bundles)
                    }
                } catch (e: CancellationException) {
                    Timber.i("Cancelled tokens remote update")
                } catch (e: Throwable) {
                    Timber.e(e, "Failed polling tokens")
                }
            } else {
                Timber.d("Skipping eth tokens auto-update")
            }
        }
    }
}
