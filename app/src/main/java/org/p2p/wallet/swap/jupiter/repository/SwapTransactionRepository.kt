package org.p2p.wallet.swap.jupiter.repository

import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.swap.jupiter.repository.model.SwapRoute
import org.p2p.wallet.utils.Base58String

interface SwapTransactionRepository {
    suspend fun createSwapTransactionForRoute(route: SwapRoute, userPublicKey: Base58String): Base64String
}
