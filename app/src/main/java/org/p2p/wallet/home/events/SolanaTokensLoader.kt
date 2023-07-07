package org.p2p.wallet.home.events

import timber.log.Timber
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.network.ConnectionManager
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.utils.toPublicKey

class SolanaTokensLoader(
    private val homeInteractor: HomeInteractor,
    private val appScope: AppScope,
    private val tokenKeyProvider: TokenKeyProvider,
    private val connectionManager: ConnectionManager,
    environmentManager: NetworkEnvironmentManager
) : AppLoader {

    init {
        observeConnectionStatus()
        environmentManager.addEnvironmentListener(this::class) {
            loadTokens()
        }
    }

    override suspend fun onLoad() {
        loadTokens()
    }

    override suspend fun onRefresh() {
        loadTokens()
    }

    private fun observeConnectionStatus() {
        appScope.launch {
            connectionManager.connectionStatus.collect { hasConnection ->
                if (hasConnection) {
                    loadTokens()
                }
            }
        }
    }

    private fun loadTokens() {
        appScope.launch {
            try {
                val tokens = homeInteractor.loadUserTokensAndUpdateLocal(
                    tokenKeyProvider.publicKey.toPublicKey()
                )
                homeInteractor.loadUserRates(tokens)
            } catch (e: CancellationException) {
                Timber.d("Loading sol tokens job cancelled")
            } catch (e: UnknownHostException) {
                Timber.d("Cannot load sol tokens: no internet")
            } catch (t: Throwable) {
                Timber.e(t, "Error on loading sol tokens")
            } finally {
            }
        }
    }
}
