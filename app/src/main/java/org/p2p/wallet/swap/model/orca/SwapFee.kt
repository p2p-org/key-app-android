package org.p2p.wallet.swap.model.orca

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.asApproximateUsd
import java.math.BigDecimal

class SwapFee(
    val isFreeTransactionAvailable: Boolean,
    val accountCreationToken: String?,
    val accountCreationFee: BigDecimal?,
    val accountCreationFeeUsd: BigDecimal?,
    val feePayerToken: Token.Active
) {

    val commonFee: String?
        get() = accountCreationFee?.let { "$it $approxFeeUsd" }

    val approxFeeUsd: String
        get() = accountCreationFeeUsd?.asApproximateUsd().orEmpty()

    val commonTransactionFee: String?
        get() = accountCreationFee?.toPlainString()

    val transactionFeeString: String?
        get() = accountCreationFee?.let { "$it $feePayerToken" }
}
