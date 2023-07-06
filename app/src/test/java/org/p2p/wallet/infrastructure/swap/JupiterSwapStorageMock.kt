package org.p2p.wallet.infrastructure.swap

import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.routes.JupiterAvailableSwapRoutesMap
import org.p2p.core.crypto.Base58String

class JupiterSwapStorageMock(
    override var savedTokenAMint: Base58String? = null,
    override var savedTokenBMint: Base58String? = null,
    override var routesFetchDateMillis: Long? = null,
    override var routesMap: JupiterAvailableSwapRoutesMap? = null,
    override var swapTokensFetchDateMillis: Long? = null,
    override var swapTokens: List<JupiterSwapToken>? = null
) : JupiterSwapStorageContract {
    override fun clear() {
        savedTokenAMint = null
        savedTokenBMint = null
        routesFetchDateMillis = null
        routesMap = null
        swapTokensFetchDateMillis = null
        swapTokens = null
    }
}
