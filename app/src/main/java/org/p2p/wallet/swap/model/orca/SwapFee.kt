package org.p2p.wallet.swap.model.orca

import org.p2p.wallet.utils.asApproximateUsd
import java.math.BigDecimal
import java.math.BigInteger

class SwapFee constructor(
    val isFreeTransactionAvailable: Boolean,
    val accountCreationToken: String?,
    val accountCreationFee: BigDecimal?,
    val accountCreationFeeUsd: BigDecimal?,
    val transactionFee: BigDecimal?,
    val transactionFeeUsd: BigDecimal?,
    val feePayerToken: String,
    val totalLamports: BigInteger
) {

    val commonFee: String?
        get() = accountCreationFee?.let { "$it $approxFeeUsd" }

    val approxFeeUsd: String
        get() = accountCreationFeeUsd?.asApproximateUsd().orEmpty()

    val commonTransactionFee: String?
        get() = accountCreationFee?.let { "$it $approxTransactionFeeUsd" }

    val approxTransactionFeeUsd: String
        get() = transactionFeeUsd?.asApproximateUsd().orEmpty()

    val transactionFeeString: String?
        get() = accountCreationFee?.let { "$it $feePayerToken" }
}
