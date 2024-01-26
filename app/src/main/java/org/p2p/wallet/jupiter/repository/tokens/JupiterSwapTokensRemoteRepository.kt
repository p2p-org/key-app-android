package org.p2p.wallet.jupiter.repository.tokens

import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
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

    private val getAllMutex = Mutex()

    @OptIn(ExperimentalTime::class)
    override suspend fun getTokens(): List<JupiterSwapToken> = withContext(dispatchers.computation) {
        // avoid parallel loading
        getAllMutex.withLock {
            if (isCacheCanBeUsed()) {
                Timber.i("Cache is valid, using cache")
                return@withLock daoDelegate.getAllTokens().also {
                    Timber.d("Cache has come")
                }
            }

            val tokens = async { api.getSwapTokens() }

            val measuredResult = measureTimedValue {
                daoDelegate.insertSwapTokens(tokens.await())
                    .also { swapStorage.swapTokensFetchDateMillis = System.currentTimeMillis() }
            }
            Timber.i("Insertion was ${measuredResult.duration.inWholeSeconds}")
            measuredResult.value
        }
    }

    // temp solution, we don't check is token swappable right now
    override suspend fun getSwappableTokens(sourceTokenMint: Base58String): List<JupiterSwapToken> = getTokens()

    private fun isCacheCanBeUsed(): Boolean {
        val fetchTokensDate = swapStorage.swapTokensFetchDateMillis ?: return false
        val now = System.currentTimeMillis()
        return (now - fetchTokensDate) <= TimeUnit.DAYS.toMillis(1) // check day has passed
    }

    override suspend fun findTokensExcludingMints(mintsToExclude: Set<Base58String>): List<JupiterSwapToken> {
        if (mintsToExclude.isEmpty()) return emptyList()

        ensureCache()
        val mints = mintsToExclude.map { it.base58Value }.toSet()
        return daoDelegate.findTokensExcludingMints(mints)
    }

    override suspend fun findTokensIncludingMints(mintsToInclude: Set<Base58String>): List<JupiterSwapToken> {
        if (mintsToInclude.isEmpty()) return emptyList()

        ensureCache()
        val mints = mintsToInclude.map { it.base58Value }.toSet()
        return daoDelegate.findTokensByMints(mints)
    }

    override suspend fun findTokenByMint(mintAddress: Base58String): JupiterSwapToken? {
        ensureCache()
        return daoDelegate.findTokenByMint(mintAddress)
    }

    override suspend fun requireTokenByMint(mintAddress: Base58String): JupiterSwapToken {
        return findTokenByMint(mintAddress) ?: error("Token $mintAddress not found in jupiter tokens")
    }

    override suspend fun requireUsdc(): JupiterSwapToken {
        return findTokenByMint(Constants.USDC_MINT.toBase58Instance())
            ?: findTokenBySymbol(Constants.USDC_SYMBOL)
            ?: error("USDC token not found in jupiter tokens")
    }

    override suspend fun requireWrappedSol(): JupiterSwapToken {
        return findTokenByMint(Constants.WRAPPED_SOL_MINT.toBase58Instance())
            ?: error("Wrapped SOL token not found in jupiter tokens")
    }

    override suspend fun findTokenBySymbol(symbol: String): JupiterSwapToken? {
        ensureCache()
        return daoDelegate.findTokenBySymbol(symbol.trim().lowercase())
    }

    override suspend fun filterIntersectedTokens(userTokens: List<Token.Active>): List<Token.Active> {
        val mintsToFind = userTokens.map { it.mintAddress.toBase58Instance() }.toSet()
        val foundTokens = findTokensIncludingMints(mintsToFind)
        return userTokens.filter { token ->
            foundTokens.any { it.tokenMint.base58Value == token.mintAddress }
        }
    }

    override suspend fun searchTokens(mintAddressOrSymbol: String): List<JupiterSwapToken> {
        ensureCache()
        return daoDelegate.searchTokens(mintAddressOrSymbol)
            .sortedByDescending(JupiterSwapToken::isStrictToken) // show strict tokens first
    }

    override suspend fun searchTokensInSwappable(
        mintAddressOrSymbol: String,
        sourceTokenMint: Base58String
    ): List<JupiterSwapToken> {
        ensureCache()
        return daoDelegate.searchTokens(mintAddressOrSymbol)
    }

    private suspend fun ensureCache() {
        if (!isCacheCanBeUsed()) {
            getTokens()
        }
    }
}
