package org.p2p.wallet.solend.model

import org.p2p.wallet.feerelayer.model.TokenAccount
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.scaleLong
import java.math.BigDecimal

data class SolendFee(
    val symbol: String,
    val usdRate: BigDecimal,
    val decimals: Int,
    val fee: SolendTokenFee,
    val feePayer: TokenAccount
) {

    fun getTransferFeeInDecimals(): BigDecimal = if (fee.rent.isNotZero()) {
        fee.transaction
            .fromLamports(decimals)
            .scaleLong()
    } else {
        BigDecimal.ZERO
    }

    fun getRentFeeInDecimals(): BigDecimal = if (fee.rent.isNotZero()) {
        fee.rent
            .fromLamports(decimals)
            .scaleLong()
    } else {
        BigDecimal.ZERO
    }
}
