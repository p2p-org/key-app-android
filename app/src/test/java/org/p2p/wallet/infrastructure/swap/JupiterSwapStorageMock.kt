package org.p2p.wallet.infrastructure.swap

import org.p2p.core.crypto.Base58String

class JupiterSwapStorageMock(
    override var savedTokenAMint: Base58String? = null,
    override var savedTokenBMint: Base58String? = null,
    override var swapTokensFetchDateMillis: Long? = null,
) : JupiterSwapStorageContract {
    override fun clear() {
        savedTokenAMint = null
        savedTokenBMint = null
        swapTokensFetchDateMillis = null
    }
}
