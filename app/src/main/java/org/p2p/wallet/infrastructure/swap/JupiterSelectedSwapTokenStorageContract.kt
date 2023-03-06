package org.p2p.wallet.infrastructure.swap

import org.p2p.wallet.utils.Base58String

interface JupiterSelectedSwapTokenStorageContract {
    var savedTokenAMint: Base58String?
    var savedTokenBMint: Base58String?
}
