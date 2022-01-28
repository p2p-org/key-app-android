package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.Transaction

class PreparedParams(
    val swapData: TopUpSwap,
    val transaction: Transaction,
    val feeAmount: FeeAmount,
    val transferAuthorityAccount: Account
)