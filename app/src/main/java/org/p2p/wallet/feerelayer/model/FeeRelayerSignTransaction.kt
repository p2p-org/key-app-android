package org.p2p.wallet.feerelayer.model

import org.p2p.core.crypto.Base64String
import org.p2p.core.crypto.Base58String

data class FeeRelayerSignTransaction(
    val signature: Base58String,
    val transaction: Base64String,
)
