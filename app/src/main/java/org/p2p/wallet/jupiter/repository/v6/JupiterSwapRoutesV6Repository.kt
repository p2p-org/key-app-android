package org.p2p.wallet.jupiter.repository.v6

import org.p2p.core.crypto.Base58String
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6

interface JupiterSwapRoutesV6Repository {
    suspend fun getSwapRoutesForSwapPair(
        jupiterSwapPair: JupiterSwapPair,
        userPublicKey: Base58String,
        shouldValidateRoute: Boolean
    ): JupiterSwapRouteV6?
}
