package org.p2p.wallet.striga.offramp.models

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.utils.STRIGA_FIAT_DECIMALS
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId

data class StrigaOffRampToken(
    val totalAmount: BigInteger,
    val walletId: StrigaWalletId,
    val accountId: StrigaAccountId,
) {
    val amountToWithdraw: BigDecimal
        get() = totalAmount.fromLamports(STRIGA_FIAT_DECIMALS)
}
