package org.p2p.wallet.restore.model

import java.math.BigDecimal
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.crypto.DerivationPath

data class DerivableAccount(
    val path: DerivationPath,
    val account: Account,
    val totalInSol: BigDecimal,
    val totalInUsd: BigDecimal? = null
)
