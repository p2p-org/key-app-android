package org.p2p.wallet.jupiter.repository.transaction

import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.utils.Base58String

interface JupiterSwapTransactionRepository {
    suspend fun createSwapTransactionForRoute(route: JupiterSwapRoute, userPublicKey: Base58String): Base64String
}
