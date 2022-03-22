package org.p2p.wallet.swap.interactor.orca

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.model.orca.OrcaPool
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
        val fromTokenName = getTokenFromMint(fromMint)?.first ?: throw IllegalStateException("Token not found")
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
        val result = currentRoutes
            .mapNotNull {
                if (it.size > 2) return@mapNotNull null // FIXME: Support more than 2 paths later
                orcaRouteInteractor.getPools(
                    infoPools = info?.pools,
                    route = it,
                    fromTokenName = fromTokenName,
                    toTokenName = toTokenName
                ) as MutableList
            }
        return result
    }

    // Find best pool to swap from input amount
    fun findBestPoolsPairForInputAmount(
        inputAmount: BigInteger,
        poolsPairs: List<OrcaPoolsPair>
    ): OrcaPoolsPair? {
        if (poolsPairs.isEmpty()) return null

        var bestPools = mutableListOf<OrcaPool>()
        var bestEstimatedAmount: BigInteger = BigInteger.ZERO

        for (pair in poolsPairs) {
            val estimatedAmount = pair.getOutputAmount(inputAmount) ?: continue
            if (estimatedAmount > bestEstimatedAmount) {
                bestEstimatedAmount = estimatedAmount
                bestPools = pair
            }
        }

        return bestPools
    }

    // Find best pool to swap from estimated amount
    fun findBestPoolsPairForEstimatedAmount(
        estimatedAmount: BigInteger,
        poolsPairs: List<OrcaPoolsPair>
    ): OrcaPoolsPair? {
        if (poolsPairs.isEmpty()) return null

        var bestPools = mutableListOf<OrcaPool>()
        var bestInputAmount: BigInteger = Int.MAX_VALUE.toBigInteger()

        for (pair in poolsPairs) {
            val inputAmount = pair.getInputAmount(estimatedAmount) ?: continue
            if (inputAmount < bestInputAmount) {
                bestInputAmount = inputAmount
                bestPools = pair
            }
        }

        return bestPools
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
        val info = orcaInfoInteractor.getInfo() ?: throw IllegalStateException("Swap info missing")
        // if fromToken isn't selected
        if (fromTokenName.isNullOrEmpty()) return mutableMapOf()

        // if toToken isn't selected
        if (toTokenName == null) {
            // get all routes that have token A
            return info.routes.filter { it.key.split("/").contains(fromTokenName) } as MutableMap
        }

        // get routes with fromToken and toToken
        val pair = listOf(fromTokenName, toTokenName)
        val validRoutesNames = listOf(
            pair.joinToString("/"),
            pair.reversed().joinToString("/")
        )
        return info.routes.filter { validRoutesNames.contains(it.key) } as MutableMap
    }

    private suspend fun mapTokensForDestination(orcaTokens: List<OrcaToken>): List<Token> {
        val userTokens = userInteractor.getUserTokens()
        val publicKey = tokenKeyProvider.publicKey
        val allTokens = orcaTokens
            .mapNotNull { orcaToken ->
                val userToken = userTokens.find { it.mintAddress == orcaToken.mint }
                return@mapNotNull when {
                    userToken != null ->
                        if (userToken.isSOL && userToken.publicKey != publicKey) null else userToken
                    else ->
                        userInteractor.findTokenData(orcaToken.mint)
                }
            }
            .sortedWith(TokenComparator())

        return allTokens
    }
}
