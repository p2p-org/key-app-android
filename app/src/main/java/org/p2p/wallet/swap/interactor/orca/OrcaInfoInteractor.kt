package org.p2p.wallet.swap.interactor.orca

import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.model.orca.OrcaPools
import org.p2p.wallet.swap.model.orca.OrcaRoute
import org.p2p.wallet.swap.model.orca.OrcaRoutes
import org.p2p.wallet.swap.model.orca.OrcaSwapInfo
import org.p2p.wallet.swap.model.orca.OrcaTokens
import org.p2p.wallet.swap.repository.OrcaSwapRepository
import timber.log.Timber
import kotlinx.coroutines.withContext

// TODO: Move cache to repositories and remove singleton from this class in koin tree
class OrcaInfoInteractor(
    private val orcaRepository: OrcaSwapRepository,
    private val dispatchers: CoroutineDispatchers
) {

    private var info: OrcaSwapInfo? = null

    fun getInfo(): OrcaSwapInfo? = info

    // Prepare all needed infos for swapping
    suspend fun load() {
        withContext(dispatchers.io) {
            if (info != null) return@withContext

            val configs = orcaRepository.loadOrcaConfigs()

            val tokens = configs.tokens
            val pools = configs.pools
            val programIds = configs.programId

            val routes = findAllAvailableRoutes(tokens, pools)

            val tokenNames = mutableMapOf<String, String>()
            tokens.forEach { (key, value) -> tokenNames[value.mint] = key }

            Timber.d("Orca swap info loaded: $tokenNames")

            info = OrcaSwapInfo(
                routes = routes,
                tokens = tokens,
                pools = pools,
                programIds = programIds,
                tokenNames = tokenNames
            )
        }
    }

    private fun findAllAvailableRoutes(tokens: OrcaTokens, pools: OrcaPools): OrcaRoutes {
        val filteredTokens = tokens.filter { it.value.poolToken != true }.map { it.key }
        val pairs = getPairs(filteredTokens)
        return getAllRoutes(pairs, pools)
    }

    private fun getPairs(tokens: List<String>): List<List<String>> {
        val pairs: MutableList<List<String>> = mutableListOf()

        if (tokens.isEmpty()) return pairs

        for (i in 0 until tokens.size - 1) {
            for (j in i + 1 until tokens.size) {
                val tokenA = tokens[i]
                val tokenB = tokens[j]

                pairs.add(orderTokenPair(tokenA, tokenB))
            }
        }

        return pairs
    }

    private fun getAllRoutes(pairs: List<List<String>>, pools: OrcaPools): OrcaRoutes {
        val routes: OrcaRoutes = mutableMapOf()
        pairs.forEach { pair ->
            val tokenA = pair.firstOrNull()
            val tokenB = pair.lastOrNull()

            if (tokenA.isNullOrEmpty() || tokenB.isNullOrEmpty()) return@forEach

            routes[getTradeId(tokenA, tokenB)] = getRoutes(tokenA, tokenB, pools)
        }
        return routes
    }

    private fun getTradeId(tokenX: String, tokenY: String): String =
        orderTokenPair(tokenX, tokenY).joinToString("/")

    private fun orderTokenPair(tokenX: String, tokenY: String): List<String> {
        return if (tokenX == "USDC" && tokenY == "USDT") {
            listOf(tokenX, tokenY)
        } else if (tokenY == "USDC" && tokenX == "USDT") {
            listOf(tokenY, tokenX)
        } else if (tokenY == "USDC" || tokenY == "USDT") {
            listOf(tokenX, tokenY)
        } else if (tokenX == "USDC" || tokenX == "USDT") {
            listOf(tokenY, tokenX)
        } else if (tokenX < tokenY) {
            listOf(tokenX, tokenY)
        } else {
            listOf(tokenY, tokenX)
        }
    }

    private fun getRoutes(tokenA: String, tokenB: String, pools: OrcaPools): List<OrcaRoute> {
        val routes = mutableListOf<OrcaRoute>()

        // Find all pools that contain the same tokens.
        // Checking tokenAName and tokenBName will find Stable pools.
        pools.forEach { (poolId, poolConfig) ->
            if ((poolConfig.tokenAName == tokenA && poolConfig.tokenBName == tokenB) ||
                (poolConfig.tokenAName == tokenB && poolConfig.tokenBName == tokenA)
            ) {
                routes.add(mutableListOf(poolId))
            }
        }

        // Find all pools that contain the first token but not the second
        val filteredPools = pools
            .filter {
                (it.value.tokenAName == tokenA && it.value.tokenBName != tokenB) ||
                    (it.value.tokenBName == tokenA && it.value.tokenAName != tokenB)
            }

        val firstLegPools = mutableMapOf<String, String>()

        filteredPools.forEach { pool ->
            firstLegPools[pool.key] = if (pool.value.tokenBName == tokenA) {
                pool.value.tokenAName
            } else {
                pool.value.tokenBName
            }
        }

        // Find all routes that can include firstLegPool and a second pool.
        firstLegPools.forEach { (firstLegPoolId, intermediateTokenName) ->
            pools.forEach { (secondLegPoolId, poolConfig) ->
                if ((poolConfig.tokenAName == intermediateTokenName && poolConfig.tokenBName == tokenB) ||
                    (poolConfig.tokenBName == intermediateTokenName && poolConfig.tokenAName == tokenB)
                ) {
                    routes.add(listOf(firstLegPoolId, secondLegPoolId))
                }
            }
        }

        return routes
    }
}
