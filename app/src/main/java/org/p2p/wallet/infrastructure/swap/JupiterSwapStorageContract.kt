package org.p2p.wallet.infrastructure.swap

import org.p2p.core.crypto.Base58String

interface JupiterSwapStorageContract {
    var savedTokenAMint: Base58String?
    var savedTokenBMint: Base58String?

    var swapTokensFetchDateMillis: Long?

    fun clear()
}
