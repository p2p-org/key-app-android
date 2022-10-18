package org.p2p.wallet.solend.model

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize

sealed interface SolendDepositToken : Parcelable {
    val tokenName: String
    val tokenSymbol: String
    val usdRate: BigDecimal
    val iconUrl: String?
    val supplyInterest: BigDecimal?
    val decimals: Int
    val availableTokensForDeposit: BigDecimal

    @Parcelize
    data class Active constructor(
        override val tokenName: String,
        override val tokenSymbol: String,
        override val usdRate: BigDecimal,
        override val iconUrl: String?,
        override val supplyInterest: BigDecimal?,
        override val decimals: Int,
        override val availableTokensForDeposit: BigDecimal,
        val depositAmount: BigDecimal,
        val usdAmount: BigDecimal,
    ) : SolendDepositToken

    @Parcelize
    data class Inactive constructor(
        override val tokenName: String,
        override val tokenSymbol: String,
        override val usdRate: BigDecimal,
        override val iconUrl: String?,
        override val supplyInterest: BigDecimal?,
        override val decimals: Int,
        override val availableTokensForDeposit: BigDecimal,
    ) : SolendDepositToken
}
