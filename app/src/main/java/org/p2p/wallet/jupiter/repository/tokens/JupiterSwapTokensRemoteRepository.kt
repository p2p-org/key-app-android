package org.p2p.wallet.jupiter.repository.tokens

import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
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

    @OptIn(ExperimentalTime::class)
    override suspend fun getTokens(): List<JupiterSwapToken> = withContext(dispatchers.computation) {
        if (isCacheCanBeUsed()) {
            Timber.i("Cache is valid, using cache")
            return@withContext daoDelegate.getAllTokens()
        }

        val routes = async { api.getSwapRoutesMapStreaming() }
        val tokens = async { api.getSwapTokens() }

        val measuredResult = measureTimedValue {
            daoDelegate.insertSwapTokens(routes.await(), tokens.await())
                .also { swapStorage.swapTokensFetchDateMillis = System.currentTimeMillis() }
        }
        Timber.e("Insertion was ${measuredResult.duration.inWholeSeconds}")
        measuredResult.value
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
