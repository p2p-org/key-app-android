package org.p2p.wallet.feerelayer.model

import android.os.Parcelable
import org.p2p.solanaj.core.FeeAmount
import java.math.BigInteger
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeeRelayerFee constructor(
    val transactionFeeInSol: BigInteger,
    val accountCreationFeeInSol: BigInteger,

    val transactionFeeInSpl: BigInteger,
    val accountCreationFeeInSpl: BigInteger,

    val expectedFee: FeeAmount
) : Parcelable {

    val totalInSol: BigInteger
        get() = transactionFeeInSol + accountCreationFeeInSol

    val totalInSpl: BigInteger
        get() = transactionFeeInSpl + accountCreationFeeInSpl
}
