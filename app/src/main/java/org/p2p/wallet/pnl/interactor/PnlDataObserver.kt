package org.p2p.wallet.pnl.interactor

import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.p2p.core.common.di.AppScope
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.common.feature_toggles.toggles.remote.PnlEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.pnl.repository.PnlRepository
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState

/**
 * Flow of this observer.
 * Required features:
 * - update not often than every 5 minutes
 * - updates for PNL data is working in parallel with token service, so tokens can be updated sooner then PNL
 * - force refresh at any moment
 *
 * Flow:
 * 1. initially load user tokens using TokenServiceCoordinator
 * 2.
 *   - observe user tokens and save them to the local variable
 *   - start PNL observer first time for the entire app
 * 3. if user pulled the refresher - restart PNL observer and refresh data for the last known tokens
 *
 * Potential problems:
 * - tokens might be updated sooner then PNL data so PNL won't be displayed until 5 minutes passed
 *   or pull-to-refresh is triggered
 */
class PnlDataObserver(
    private val appScope: AppScope,
    private val pnlRepository: PnlRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val pnlEnabledFeatureToggle: PnlEnabledFeatureToggle
) {

    private companion object {
        val POLL_INTERVAL = 5.minutes
    }

    private var updaterJob: Job? = null
    private val tokenMints = mutableListOf<Base58String>()
    private val tokenMintsMutex = Mutex()
    private val _pnlState = MutableStateFlow<PnlDataState>(PnlDataState.Idle)
    val pnlState = _pnlState.asStateFlow()

    fun canBeStarted(): Boolean = updaterJob != null

    fun start() {
        if (updaterJob != null || !pnlEnabledFeatureToggle.isFeatureEnabled) return
        _pnlState.value = PnlDataState.Loading
        launchJob()
    }

    fun restartAndRefresh() {
        stop()
        launchJob()
    }

    fun stop() {
        updaterJob?.cancel()
        updaterJob = null
    }

    private fun launchJob() {
        updaterJob = appScope.launch {
            // firstly, get current state, because observer may return actual tokens later
            tokenServiceCoordinator.observeLastState().value.extractTokenMints()
            // then subscribe on updates
            tokenServiceCoordinator.observeUserTokens()
                .filterIsInstance<UserTokensState.Loaded>()
                .onEach { it.extractTokenMints() }
                .launchIn(this)

            while (isActive) {
                try {
                    val pnlData = pnlRepository.getPnlData(tokenKeyProvider.publicKeyBase58, tokenMints)
                    _pnlState.emit(PnlDataState.Loaded(pnlData))
                } catch (e: Throwable) {
                    Timber.e(e, "Unable to get pnl data")
                    _pnlState.emit(
                        PnlDataState.Error(e)
                    )
                }
                delay(POLL_INTERVAL)
            }
        }
    }

    private suspend fun UserTokensState.extractTokenMints() {
        if (this is UserTokensState.Loaded) {
            val newMints = solTokens.map { token -> token.mintAddressB58 }
            tokenMintsMutex.withLock {
                tokenMints.clear()
                tokenMints.addAll(newMints)
            }
        }
    }
}
