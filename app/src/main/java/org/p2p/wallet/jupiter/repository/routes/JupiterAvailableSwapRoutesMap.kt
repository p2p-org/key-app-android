package org.p2p.wallet.jupiter.repository.routes

import org.p2p.wallet.utils.Base58String

class JupiterAvailableSwapRoutesMap(
    val tokenMints: List<Base58String>,
    val allRoutes: Map<Int, List<Int>>
)
