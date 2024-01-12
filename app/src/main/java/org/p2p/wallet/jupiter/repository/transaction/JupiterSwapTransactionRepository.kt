package org.p2p.wallet.jupiter.repository.transaction

import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6

interface JupiterSwapTransactionRepository {
    suspend fun createSwapTransactionForRoute(route: JupiterSwapRoute, userPublicKey: Base58String): Base64String
    suspend fun createSwapTransactionForRoute(route: JupiterSwapRouteV6, userPublicKey: Base58String): Base64String
}
