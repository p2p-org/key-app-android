package org.p2p.solanaj.core

import android.os.Parcelable
import java.math.BigInteger
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeeAmount(
    var transactionFee: BigInteger = BigInteger.ZERO,
    var accountCreationFee: BigInteger = BigInteger.ZERO
) : Parcelable {

    val totalFeeLamports: BigInteger
        get() = transactionFee + accountCreationFee
}
