package org.p2p.wallet.solend.model

import java.math.BigDecimal

sealed class SolendDepositToken(
    open val tokenName: String,
    open val tokenSymbol: String,
    open val iconUrl: String,
    open val earnApy: Float
) {

    data class Active(
        override val tokenName: String,
        override val tokenSymbol: String,
        override val iconUrl: String,
        override val earnApy: Float,
        val depositAmount: BigDecimal
    ) : SolendDepositToken(tokenName, tokenSymbol, iconUrl, earnApy)

    data class Inactive(
        override val tokenName: String,
        override val tokenSymbol: String,
        override val iconUrl: String,
        override val earnApy: Float
    ) : SolendDepositToken(tokenName, tokenSymbol, iconUrl, earnApy)
}
