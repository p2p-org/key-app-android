package com.p2p.wallet.swap.orca.model

import com.p2p.wallet.swap.model.AccountBalance

data class ValidOrcaPool(
    val orcaPool: OrcaPool,
    val balanceA: AccountBalance,
    val balanceB: AccountBalance
)