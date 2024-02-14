package org.p2p.wallet.feerelayer.model

import android.os.Parcelable
import java.math.BigInteger
import kotlinx.parcelize.Parcelize
import org.p2p.solanaj.core.FeeAmount

@Parcelize
data class FeeRelayerFee(
    val transactionFeeInSol: BigInteger,
    val accountCreationFeeInSol: BigInteger,

    val transactionFeeInFeePayerToken: BigInteger,
    val accountCreationFeeInFeePayerToken: BigInteger,

    val transactionFeeInSourceToken: BigInteger,
    val accountCreateFeeInSourceToken: BigInteger,

    val expectedFee: FeeAmount
) : Parcelable {

    constructor(
        feesInSol: FeeAmount,
        feesInFeePayerToken: FeeAmount,
        feesInSourceToken: FeeAmount,
        expectedFee: FeeAmount
    ) : this(
        transactionFeeInSol = feesInSol.transactionFee,
        accountCreationFeeInSol = feesInSol.accountCreationFee,
        transactionFeeInFeePayerToken = feesInFeePayerToken.transactionFee,
        accountCreationFeeInFeePayerToken = feesInFeePayerToken.accountCreationFee,
        transactionFeeInSourceToken = feesInSourceToken.transactionFee,
        accountCreateFeeInSourceToken = feesInSourceToken.accountCreationFee,
        expectedFee = expectedFee
    )

    val totalInSol: BigInteger
        get() = transactionFeeInSol + accountCreationFeeInSol

    val totalInFeePayerToken: BigInteger
        get() = transactionFeeInFeePayerToken + accountCreationFeeInFeePayerToken

    val totalInSourceToken: BigInteger
        get() = transactionFeeInSourceToken + accountCreateFeeInSourceToken
}
