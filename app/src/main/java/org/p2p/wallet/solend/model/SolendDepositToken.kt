package org.p2p.wallet.solend.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

sealed class SolendDepositToken(
    open val tokenName: String,
    open val tokenSymbol: String,
    open val usdRate: BigDecimal,
    open val iconUrl: String?,
    open val supplyInterest: BigDecimal?,
) : Parcelable {

    var availableTokensForDeposit: BigDecimal? = null

    @Parcelize
    data class Active constructor(
        override val tokenName: String,
        override val tokenSymbol: String,
        override val usdRate: BigDecimal,
        override val iconUrl: String?,
        override val supplyInterest: BigDecimal?,
        val depositAmount: BigDecimal,
        val usdAmount: BigDecimal,
    ) : SolendDepositToken(tokenName, tokenSymbol, usdRate, iconUrl, supplyInterest)

    @Parcelize
    data class Inactive(
        override val tokenName: String,
        override val tokenSymbol: String,
        override val usdRate: BigDecimal,
        override val iconUrl: String?,
        override val supplyInterest: BigDecimal?
    ) : SolendDepositToken(tokenName, tokenSymbol, usdRate, iconUrl, supplyInterest)
}
