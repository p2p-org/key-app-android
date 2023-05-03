package org.p2p.wallet.bridge.send.model

import java.math.BigInteger
import org.p2p.core.token.SolAddress

data class BridgeSendTransactionDetails(
    val id: String,
    val userWallet: SolAddress,
    val recipient: SolAddress,
    val amount: BigInteger,
    val fees: BridgeSendFees,
    val status: BridgeSendTransactionStatus
)
