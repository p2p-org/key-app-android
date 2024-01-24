package org.p2p.solanaj.core

import android.os.Parcelable
import java.math.BigInteger
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeeAmount(
    var transaction: BigInteger = BigInteger.ZERO,
    var accountBalances: BigInteger = BigInteger.ZERO
) : Parcelable {

    val totalFeeLamports: BigInteger
        get() = transaction + accountBalances
}
