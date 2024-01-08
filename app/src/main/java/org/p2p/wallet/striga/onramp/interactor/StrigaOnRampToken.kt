package org.p2p.wallet.striga.onramp.interactor

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.STRIGA_FIAT_DECIMALS
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.striga.wallet.models.StrigaOnchainWithdrawalFees
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId

data class StrigaOnRampToken(
    val totalAmount: BigInteger,
    val fees: StrigaOnchainWithdrawalFees,
    val tokenDetails: Token,
    val walletId: StrigaWalletId,
    val accountId: StrigaAccountId,
) {
    /**
     * Total amount minus fees converted from lamports
     */
    val claimableAmount: BigDecimal
        get() = (totalAmount - fees.totalFee).fromLamports(STRIGA_FIAT_DECIMALS)
}
