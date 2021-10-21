package com.p2p.wallet.swap.interactor.orca

import com.p2p.wallet.swap.model.AccountBalance
import com.p2p.wallet.swap.model.orca.OrcaPool
import com.p2p.wallet.swap.model.orca.OrcaPools
import com.p2p.wallet.swap.model.orca.OrcaRoute
import com.p2p.wallet.swap.model.orca.OrcaRoutes
import com.p2p.wallet.swap.model.orca.OrcaSwapInfo
import com.p2p.wallet.swap.repository.OrcaSwapRepository

class OrcaSwapPoolInteractor(
    private val orcaSwapRepository: OrcaSwapRepository
) {

    private val balancesCache = mutableMapOf<String, AccountBalance>()

    suspend fun getPools(
        infoPools: OrcaPools?,
        route: OrcaRoute,
        fromTokenName: String,
        toTokenName: String
    ): List<OrcaPool> {
        if (route.isEmpty()) return emptyList()

        val pools = route.mapNotNull { fixedPool(infoPools, it) }.toMutableList()
        // modify orders
        if (pools.size == 2) {
            // reverse order of the 2 pools
            // Ex: Swap from SOCN -> BTC, but paths are
            // [
            //     "BTC/SOL[aquafarm]",
            //     "SOCN/SOL[stable][aquafarm]"
            // ]
            // Need to change to
            // [
            //     "SOCN/SOL[stable][aquafarm]",
            //     "BTC/SOL[aquafarm]"
            // ]

            if (pools[0].tokenAName != fromTokenName && pools[0].tokenBName != fromTokenName) {
                val temp = pools[0]
                pools[0] = pools[1]
                pools[1] = temp
            }
        }

        // reverse token A and token B in pool if needed
        for (i in 0 until pools.size) {
            if (i == 0) {
                var pool = pools[0]
                if (pool.tokenAName.fixedTokenName() != fromTokenName.fixedTokenName()) {
                    pool = pool.reversed
                }
                pools[0] = pool
            }

            if (i == 1) {
                var pool = pools[1]
                if (pool.tokenBName.fixedTokenName() != toTokenName.fixedTokenName()) {
                    pool = pool.reversed
                }
                pools[1] = pool
            }
        }

        return pools
    }

    private suspend fun fixedPool(
        infoPools: OrcaPools?,
        path: String // Ex. BTC/SOL[aquafarm][stable]){}
    ): OrcaPool? {

        val pool = infoPools?.get(path) ?: return null

        if (path.contains("[stable]")) {
            pool.isStable = true
        }

        // get balances
        var tokenABalance = pool.tokenABalance ?: balancesCache[pool.tokenAccountA.toBase58()]
        var tokenBBalance = pool.tokenBBalance ?: balancesCache[pool.tokenAccountB.toBase58()]

        if (tokenABalance == null || tokenBBalance == null) {
            tokenABalance = orcaSwapRepository.loadTokenBalance(pool.tokenAccountA)
            tokenBBalance = orcaSwapRepository.loadTokenBalance(pool.tokenAccountB)

            balancesCache[pool.tokenAccountA.toBase58()] = tokenABalance
            balancesCache[pool.tokenAccountB.toBase58()] = tokenBBalance
        }

        pool.tokenABalance = tokenABalance
        pool.tokenBBalance = tokenBBalance

        return pool
    }

    // Find possible destination token (symbol)
    // - Parameter fromTokenName: from token name (symbol)
    // - Returns: List of token symbols that can be swapped to
    fun findPossibleDestinations(
        info: OrcaSwapInfo,
        fromTokenName: String
    ): List<String> {
        return findRoutes(info, fromTokenName, null).keys
            .mapNotNull { key ->
                key.split("/").find { it != fromTokenName }
            }
            .distinct()
            .sorted()
    }

    // / Find routes for from and to token name, aka symbol
    fun findRoutes(
        info: OrcaSwapInfo,
        fromTokenName: String?,
        toTokenName: String?
    ): OrcaRoutes {
        // if fromToken isn't selected
        if (fromTokenName.isNullOrEmpty()) return mutableMapOf()

        // if toToken isn't selected
        if (toTokenName == null) {
            // get all routes that have token A
            return info.routes.filter { it.key.split("/").contains(fromTokenName) } as OrcaRoutes
        }

        // get routes with fromToken and toToken
        val pair = listOf(fromTokenName, toTokenName)
        val validRoutesNames = listOf(
            pair.joinToString("/"),
            pair.reversed().joinToString("/")
        )
        return info.routes.filter { validRoutesNames.contains(it.key) } as OrcaRoutes
    }
}

private fun String.fixedTokenName(): String = this.split("[").first()