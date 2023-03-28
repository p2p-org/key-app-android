package org.p2p.wallet.restore.model

import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.solanaj.core.Account
import java.math.BigDecimal

data class DerivableAccount(
    val path: DerivationPath,
    val account: Account,
    val total: BigDecimal,
    val totalInUsd: BigDecimal? = null
)
