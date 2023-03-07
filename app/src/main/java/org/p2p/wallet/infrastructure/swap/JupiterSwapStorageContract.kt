package org.p2p.wallet.infrastructure.swap

import org.p2p.wallet.swap.jupiter.repository.routes.JupiterAvailableSwapRoutesMap
import org.p2p.wallet.utils.Base58String

interface JupiterSwapStorageContract {
    var savedTokenAMint: Base58String?
    var savedTokenBMint: Base58String?
    var routesFetchDateMillis: Long?
    var routesMap: JupiterAvailableSwapRoutesMap?
}
