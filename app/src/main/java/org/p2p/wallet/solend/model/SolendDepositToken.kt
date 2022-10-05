package org.p2p.wallet.solend.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
sealed class SolendDepositToken(
    open val tokenName: String,
    open val tokenSymbol: String,
    open val iconUrl: String?,
    open val supplyInterest: BigDecimal
) : Parcelable {

    data class Active(
        override val tokenName: String,
        override val tokenSymbol: String,
        override val iconUrl: String?,
        override val supplyInterest: BigDecimal,
        val depositAmount: BigDecimal
    ) : SolendDepositToken(tokenName, tokenSymbol, iconUrl, supplyInterest)

    data class Inactive(
        override val tokenName: String,
        override val tokenSymbol: String,
        override val iconUrl: String?,
        override val supplyInterest: BigDecimal
    ) : SolendDepositToken(tokenName, tokenSymbol, iconUrl, supplyInterest)
}
