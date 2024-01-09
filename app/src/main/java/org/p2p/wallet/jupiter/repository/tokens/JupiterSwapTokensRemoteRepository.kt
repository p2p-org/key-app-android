package org.p2p.wallet.jupiter.repository.tokens

import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.api.SwapJupiterApi
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.tokens.db.SwapTokensDaoDelegate

internal class JupiterSwapTokensRemoteRepository(
    private val api: SwapJupiterApi,
    private val daoDelegate: SwapTokensDaoDelegate,
    private val dispatchers: CoroutineDispatchers,
    private val swapStorage: JupiterSwapStorageContract,
) : JupiterSwapTokensRepository {

    override suspend fun getTokens(): List<JupiterSwapToken> = withContext(dispatchers.computation) {
        if (isCacheCanBeUsed()) {
            Timber.i("Cache is valid, using cache")
            return@withContext daoDelegate.getAllTokens()
        }

        val routes = async { api.getSwapRoutesMap() }
        val tokens = async { api.getSwapTokens() }

        daoDelegate.insertSwapTokens(routes.await(), tokens.await())
            .also { swapStorage.swapTokensFetchDateMillis = System.currentTimeMillis() }
    }

    override suspend fun searchTokens(mintAddressOrSymbol: String): List<JupiterSwapToken> {
        return daoDelegate.searchTokens(mintAddressOrSymbol)
    }

    override suspend fun searchTokensInSwappable(
        mintAddressOrSymbol: String,
        sourceTokenMint: Base58String
    ): List<JupiterSwapToken> {
        return daoDelegate.searchTokens(mintAddressOrSymbol, sourceTokenMint)
    }

    private fun isCacheCanBeUsed(): Boolean {
        val fetchTokensDate = swapStorage.swapTokensFetchDateMillis ?: return false
        val now = System.currentTimeMillis()
        return (now - fetchTokensDate) <= TimeUnit.DAYS.toMillis(1) // check day has passed
    }
}
