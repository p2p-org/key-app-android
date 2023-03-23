package org.p2p.wallet.home.ui.main

import timber.log.Timber
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.p2p.core.token.Token
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.wallet.bridge.claim.interactor.ClaimInteractor
import org.p2p.wallet.bridge.claim.model.ClaimStatus
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.home.ui.main.models.EthereumState
import org.p2p.wallet.user.interactor.UserInteractor

private val POLLING_DELAY = 10.toDuration(DurationUnit.SECONDS)

class UserTokensPolling(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumRepository: EthereumRepository,
    private val claimInteractor: ClaimInteractor,
) {

    private val isPollingEnabled: Boolean
        get() = appFeatureFlags.isPollingEnabled.featureValue

    suspend fun startPolling(onTokensLoaded: suspend (List<Token.Active>, EthereumState) -> Unit) {
        if (isPollingEnabled) {
            try {
                while (true) {
                    delay(POLLING_DELAY.inWholeMilliseconds)
                    val newTokens = userInteractor.loadUserTokensAndUpdateLocal(fetchPrices = false)
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

    private suspend fun getEthereumState(): EthereumState {
        if (!ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            return EthereumState()
        }
        val ethereumTokens = loadEthTokens()
        val ethereumBundleStatuses = loadEthBundles()
        return EthereumState(ethereumTokens, ethereumBundleStatuses)
    }

    private suspend fun loadEthTokens(): List<Token.Eth> {
        return try {
            ethereumRepository.loadWalletTokens()
        } catch (cancelled: CancellationException) {
            Timber.i(cancelled)
            emptyList()
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Error on loading ethereumTokens")
            emptyList()
        }
    }

    private suspend fun loadEthBundles(): Map<String, ClaimStatus?> {
        return try {
            claimInteractor.getListOfEthereumBundleStatuses()
        } catch (cancelled: CancellationException) {
            Timber.i(cancelled)
            emptyList()
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Error on loading loadEthBundles")
            emptyList()
        }.associate {
            (it.token?.hex ?: ERC20Tokens.ETH.contractAddress) to it.status
        }
    }
}
