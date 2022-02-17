package org.p2p.wallet.swap.model.orca

import org.p2p.wallet.utils.asApproximateUsd
import java.math.BigDecimal

class SwapFee(
    val isFreeTransactionAvailable: Boolean,
    val accountCreationToken: String?,
    val accountCreationFee: String?,
    val accountCreationFeeUsd: BigDecimal?,
    val transactionFee: String?,
    val transactionFeeUsd: BigDecimal?
) {

    val commonFee: String?
        get() = accountCreationFee?.let { "$it $approxFeeUsd" }

    val approxFeeUsd: String
        get() = accountCreationFeeUsd?.asApproximateUsd().orEmpty()

    val commonTransactionFee: String?
        get() = accountCreationFee?.let { "$it $approxTransactionFeeUsd" }

    val approxTransactionFeeUsd: String
        get() = transactionFeeUsd?.asApproximateUsd().orEmpty()
}