package org.p2p.wallet.solend.model

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize

sealed class SolendDepositToken(
    open val tokenName: String,
    open val tokenSymbol: String,
    open val iconUrl: String?,
    open val supplyInterest: BigDecimal
) : Parcelable {

    @Parcelize
    data class Active(
        override val tokenName: String,
        override val tokenSymbol: String,
        override val iconUrl: String?,
        override val supplyInterest: BigDecimal,
        val depositAmount: BigDecimal,
        val usdAmount: BigDecimal
    ) : SolendDepositToken(tokenName, tokenSymbol, iconUrl, supplyInterest)

    @Parcelize
    data class Inactive(
        override val tokenName: String,
        override val tokenSymbol: String,
        override val iconUrl: String?,
        override val supplyInterest: BigDecimal
    ) : SolendDepositToken(tokenName, tokenSymbol, iconUrl, supplyInterest)
}
