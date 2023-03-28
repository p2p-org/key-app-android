package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.utils.Base58String

data class FeeRelayerSignTransaction(
    val signature: Base58String,
    val transaction: Base64String,
)
