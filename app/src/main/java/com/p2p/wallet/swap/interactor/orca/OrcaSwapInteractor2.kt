package com.p2p.wallet.swap.interactor.orca

import com.p2p.wallet.swap.model.orca.OrcaSwapInfo
import com.p2p.wallet.swap.model.orca.Routes

class OrcaSwapInteractor2 {

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
    ): Routes {
        // if fromToken isn't selected
        if (fromTokenName.isNullOrEmpty()) return emptyMap()

        // if toToken isn't selected
        if (toTokenName == null) {
            // get all routes that have token A
            return info.routes.filter { it.key.split("/").contains(fromTokenName) }
        }

        // get routes with fromToken and toToken
        val pair = listOf(fromTokenName, toTokenName)
        val validRoutesNames = listOf(
            pair.joinToString("/"),
            pair.reversed().joinToString("/")
        )
        return info.routes.filter { validRoutesNames.contains(it.key) }
    }
}