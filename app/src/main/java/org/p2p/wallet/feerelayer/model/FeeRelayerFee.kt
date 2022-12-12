package org.p2p.wallet.feerelayer.model

import android.os.Parcelable
import java.math.BigInteger
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeeRelayerFee constructor(
    val transactionFeeInSol: BigInteger,
    val accountCreationFeeInSol: BigInteger,

    val transactionFeeInSpl: BigInteger,
    val accountCreationFeeInSpl: BigInteger
) : Parcelable {

    val totalInSol: BigInteger
        get() = transactionFeeInSol + accountCreationFeeInSol

    val totalInSpl: BigInteger
        get() = transactionFeeInSpl + accountCreationFeeInSpl
}
