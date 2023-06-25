package org.p2p.wallet.bridge.send.model

import org.p2p.core.crypto.Base64String

data class BridgeSendTransaction(
    val transaction: Base64String,
    val message: String?,
)
