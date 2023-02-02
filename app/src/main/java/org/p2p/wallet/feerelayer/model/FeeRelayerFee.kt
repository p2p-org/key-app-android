package org.p2p.wallet.feerelayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.solanaj.core.FeeAmount
import java.math.BigInteger

@Parcelize
data class FeeRelayerFee constructor(
    val transactionFeeInSol: BigInteger,
    val accountCreationFeeInSol: BigInteger,

    val transactionFeeInSpl: BigInteger,
    val accountCreationFeeInSpl: BigInteger,

    val expectedFee: FeeAmount
) : Parcelable {

    constructor(solFeeAmount: FeeAmount, splFeeAmount: FeeAmount, expectedFee: FeeAmount) : this(
        transactionFeeInSol = solFeeAmount.accountBalances,
        accountCreationFeeInSol = solFeeAmount.transaction,
        transactionFeeInSpl = splFeeAmount.transaction,
        accountCreationFeeInSpl = splFeeAmount.accountBalances,
        expectedFee = expectedFee
    )

    val totalInSol: BigInteger
        get() = transactionFeeInSol + accountCreationFeeInSol

    val totalInSpl: BigInteger
        get() = transactionFeeInSpl + accountCreationFeeInSpl
}
