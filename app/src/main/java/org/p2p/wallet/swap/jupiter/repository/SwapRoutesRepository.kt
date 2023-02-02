package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.swap.jupiter.repository.model.SwapQuote
import org.p2p.wallet.swap.jupiter.repository.model.SwapRoute
import org.p2p.wallet.utils.Base58String

interface SwapRoutesRepository {
    suspend fun getSwapQuote(swapQuote: SwapQuote, userPublicKey: Base58String): List<SwapRoute>
}
