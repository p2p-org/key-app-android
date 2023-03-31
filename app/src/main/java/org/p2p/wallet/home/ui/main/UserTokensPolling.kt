package org.p2p.wallet.home.ui.main

import timber.log.Timber
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.p2p.core.token.Token
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.bridge.claim.model.ClaimStatus
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.home.ui.main.models.EthereumHomeState
import org.p2p.wallet.user.interactor.UserInteractor

private val POLLING_DELAY = 10.toDuration(DurationUnit.SECONDS)

class UserTokensPolling(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor
) {

    private val isPollingEnabled: Boolean
        get() = appFeatureFlags.isPollingEnabled.featureValue

    suspend fun startPolling(onTokensLoaded: suspend (List<Token.Active>, EthereumHomeState) -> Unit) {
        if (isPollingEnabled) {
            try {
                while (true) {
                    delay(POLLING_DELAY.inWholeMilliseconds)
                    val newTokens = userInteractor.loadUserTokensAndUpdateLocal()
                    val ethereumState = getEthereumState()
                    onTokensLoaded.invoke(newTokens, ethereumState)
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

    private suspend fun getEthereumState(): EthereumHomeState {
        if (!ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            return EthereumHomeState()
        }
        val ethereumTokens = loadEthTokens()
        val ethereumBundleStatuses = loadEthBundles()
        return EthereumHomeState(ethereumTokens, ethereumBundleStatuses)
    }

    private suspend fun loadEthTokens(): List<Token.Eth> {
        return try {
            ethereumInteractor.loadWalletTokens()
        } catch (cancelled: CancellationException) {
            Timber.i(cancelled)
            emptyList()
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Error on loading ethereumTokens")
            emptyList()
        }
    }

    private suspend fun loadEthBundles(): Map<String, List<ClaimStatus?>> {
        return try {
            val bundles = ethereumInteractor.getListOfEthereumBundleStatuses()
            val resultMap = mutableMapOf<String, List<ClaimStatus>>()
            val ethAddress = ERC20Tokens.ETH.contractAddress
            bundles.map { it.token?.hex ?: ethAddress }
                .forEach { token ->
                    val tokenBundles = bundles.filter { token == (it.token?.hex ?: ethAddress) }.map { it.status }
                    resultMap[token] = tokenBundles.filterNotNull()
                }
            resultMap
        } catch (cancelled: CancellationException) {
            Timber.i(cancelled)
            emptyMap()
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Error on loading loadEthBundles")
            emptyMap()
        }
    }
}
