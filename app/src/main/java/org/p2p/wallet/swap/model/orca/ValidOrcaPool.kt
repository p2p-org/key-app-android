package org.p2p.wallet.swap.model.orca

import org.p2p.wallet.swap.model.AccountBalance

data class ValidOrcaPool(
    val orcaPool: OrcaPool,
    val balanceA: AccountBalance,
    val balanceB: AccountBalance
)