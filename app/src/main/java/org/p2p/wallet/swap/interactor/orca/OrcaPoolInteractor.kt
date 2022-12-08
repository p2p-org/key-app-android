package org.p2p.wallet.swap.interactor.orca

import org.p2p.core.token.Token
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getInputAmount
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getOutputAmount
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaRoutes
import org.p2p.wallet.swap.model.orca.OrcaToken
import org.p2p.wallet.user.interactor.UserInteractor
import java.math.BigInteger

class OrcaPoolInteractor(
    private val orcaRouteInteractor: OrcaRouteInteractor,
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    /**
     * Finds possible destination token (symbol)
     * @param fromMint from token mint address
     * @returns list of token symbols that can be swapped to
     * */
    suspend fun findPossibleDestinations(
        fromMint: String
    ): List<Token> {
        val fromTokenName = getTokenFromMint(fromMint)?.first ?: error("Token not found")
        val routes = findRoutes(fromTokenName, null)
        val info = orcaInfoInteractor.getInfo()

        val orcaTokens = routes
            .keys
            .mapNotNull { key ->
                key.split("/").find { it != fromTokenName }
            }
            .distinct()
            .mapNotNull { info?.tokens?.get(it) }

        return mapTokensForDestination(orcaTokens)
    }

    // Get all tradable pools pairs for current token pair
    // - Returns: route and parsed pools
    suspend fun getTradablePoolsPairs(
        fromMint: String,
        toMint: String
    ): List<OrcaPoolsPair> {
        val fromTokenName = getTokenFromMint(fromMint)?.first
        val toTokenName = getTokenFromMint(toMint)?.first
        val currentRoutes = findRoutes(fromTokenName, toTokenName).values.firstOrNull()

        if (fromTokenName.isNullOrEmpty() || toTokenName.isNullOrEmpty() || currentRoutes.isNullOrEmpty()) {
            return emptyList()
        }

        val info = orcaInfoInteractor.getInfo()

        orcaRouteInteractor.loadBalances(currentRoutes, info?.pools)

        // retrieve all routes
        val result = currentRoutes.mapNotNull {
            if (it.size > 2) return@mapNotNull null // FIXME: Support more than 2 paths later
            orcaRouteInteractor.getPools(
                infoPools = info?.pools,
                route = it,
                fromTokenName = fromTokenName,
                toTokenName = toTokenName
            ).toMutableList()
        }
        return result
    }

    // Find best pool to swap from input amount
    fun findBestPoolsPairForInputAmount(
        inputAmount: BigInteger,
        poolsPairs: List<OrcaPoolsPair>
    ): OrcaPoolsPair? {
        if (poolsPairs.isEmpty()) return null

        val sortedPoolsPair = poolsPairs.sortedWith { pair1: OrcaPoolsPair, pair2: OrcaPoolsPair ->
            val estimatedAmount1 = pair1.getOutputAmount(inputAmount) ?: BigInteger.ZERO
            val estimatedAmount2 = pair2.getOutputAmount(inputAmount) ?: BigInteger.ZERO

            when {
                estimatedAmount1 > estimatedAmount2 -> 1
                estimatedAmount2 > estimatedAmount1 -> -1
                else -> 0
            }
        }
            .filter { orcaPool ->
                val outputAmount = orcaPool.getOutputAmount(inputAmount) ?: BigInteger.ZERO
                orcaPool.isNotEmpty() && orcaPool.size <= 2 && outputAmount > BigInteger.ZERO
            }

        // TODO (from ios): - Think about better solution!
        // For some case when swaping small amount (how small?) which involved BTC or ETH
        // For example: USDC -> wstETH -> stSOL
        // The transaction might be rejected because the input amount and output amount of intermediary token (wstETH) is too small
        // To temporarily fix this issue, prefers direct route or transitive route without ETH, BTC
        val directSwapPool = sortedPoolsPair.firstOrNull { it.size == 1 }
        return directSwapPool ?: poolsPairs.first()
    }

    // Find best pool to swap from estimated amount
    fun findBestPoolsPairForEstimatedAmount(
        estimatedAmount: BigInteger,
        poolsPairs: List<OrcaPoolsPair>
    ): OrcaPoolsPair? {
        if (poolsPairs.isEmpty()) return null

        val sortedPoolsPair = poolsPairs.sortedWith { pair1: OrcaPoolsPair, pair2: OrcaPoolsPair ->
            val inputAmount1 = pair1.getInputAmount(estimatedAmount) ?: BigInteger.ZERO
            val inputAmount2 = pair2.getInputAmount(estimatedAmount) ?: BigInteger.ZERO

            when {
                inputAmount1 < inputAmount2 -> 1
                inputAmount1 > inputAmount2 -> -1
                else -> 0
            }
        }
            .filter { orcaPool ->
                val outputAmount = orcaPool.getInputAmount(estimatedAmount) ?: BigInteger.ZERO
                orcaPool.isNotEmpty() && orcaPool.size <= 2 && outputAmount > BigInteger.ZERO
            }

        // TODO (from ios): - Think about better solution!
        // For some case when swaping small amount (how small?) which involved BTC or ETH
        // For example: USDC -> wstETH -> stSOL
        // The transaction might be rejected because the input amount and output amount of intermediary token (wstETH) is too small
        // To temporarily fix this issue, prefers direct route or transitive route without ETH, BTC
        val directSwapPool = sortedPoolsPair.firstOrNull { it.size == 1 }
        return directSwapPool ?: sortedPoolsPair.first()
    }

    // Map mint to token info
    fun getTokenFromMint(mint: String): Pair<String, OrcaToken>? {
        val info = orcaInfoInteractor.getInfo()
        val key = info?.tokens?.filterValues { it.mint == mint }?.keys?.firstOrNull() ?: return null
        val tokenInfo = info.tokens[key] ?: return null
        return key to tokenInfo
    }

    // Find routes for from and to token name, aka symbol
    private fun findRoutes(
        fromTokenName: String?,
        toTokenName: String?
    ): OrcaRoutes {
        val swapInfo = orcaInfoInteractor.getInfo() ?: error("Swap info missing")
        // if fromToken isn't selected
        if (fromTokenName.isNullOrEmpty()) return mutableMapOf()

        // if toToken isn't selected
        if (toTokenName == null) {
            // get all routes that have token A
            return swapInfo.routes.filter { it.key.split("/").contains(fromTokenName) }.toMutableMap()
        }

        // get routes with fromToken and toToken
        val pair = listOf(fromTokenName, toTokenName)
        // example: ["SOL/USDC", "USDC/SOL"]
        val validRoutesNames = listOf(
            pair.joinToString("/"),
            pair.reversed().joinToString("/")
        )
        return swapInfo.routes.filter { validRoutesNames.contains(it.key) }.toMutableMap()
    }

    private suspend fun mapTokensForDestination(orcaTokens: List<OrcaToken>): List<Token> {
        val userTokens = userInteractor.getUserTokens()
        val publicKey = tokenKeyProvider.publicKey
        val allTokens = orcaTokens.mapNotNull { orcaToken ->
            val userToken = userTokens.find { it.mintAddress == orcaToken.mint }
            if (userToken != null) {
                userToken.takeUnless { userToken.isSOL && userToken.publicKey != publicKey }
            } else {
                userInteractor.findTokenData(orcaToken.mint)
            }
        }
            .sortedWith(TokenComparator())

        return allTokens
    }
}
