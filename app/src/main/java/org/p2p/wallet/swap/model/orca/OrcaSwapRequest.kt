package org.p2p.wallet.swap.model.orca

import org.p2p.wallet.swap.model.AccountBalance
import java.math.BigInteger

data class OrcaSwapRequest(
    val pool: OrcaPool,
    val slippage: Double,
    val amount: BigInteger,
    val balanceA: AccountBalance,
    val balanceB: AccountBalance
)