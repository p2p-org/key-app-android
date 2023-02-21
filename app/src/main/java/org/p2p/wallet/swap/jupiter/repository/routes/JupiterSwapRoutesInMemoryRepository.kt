package org.p2p.wallet.swap.jupiter.repository.routes

import timber.log.Timber
import kotlinx.coroutines.withContext
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.utils.Base58String

internal class JupiterSwapRoutesInMemoryRepository(
    private val dispatchers: CoroutineDispatchers
) : JupiterSwapRoutesLocalRepository {

    private var cachedAllSwapRoutes: JupiterAvailableSwapRoutesMap? = null
        set(value) {
            Timber.i("allSwapRoutes updated: old:${field?.allRoutes?.size}; new=${value?.allRoutes?.size}")
            field = value
        }

    override fun setCachedSwapRoutes(swapRoutes: JupiterAvailableSwapRoutesMap) {
        this.cachedAllSwapRoutes = swapRoutes
    }

    override suspend fun getSwappableTokenMints(
        sourceTokenMint: Base58String
    ): List<Base58String> = withContext(dispatchers.computation) {
        val swapRoutes = cachedAllSwapRoutes ?: return@withContext run {
            Timber.e(IllegalStateException("allSwapRoutes are null!"))
            emptyList()
        }

        val indexOfSourceToken: Int = swapRoutes.tokenMints.indexOfFirst { sourceTokenMint == it }
        val swappableTokensIndexes: List<Int> = swapRoutes.allRoutes[indexOfSourceToken].orEmpty()
        val swappableTokensMints: List<Base58String> = swappableTokensIndexes.mapNotNull {
            swapRoutes.tokenMints.getOrNull(it)
        }
        swappableTokensMints
    }
}
