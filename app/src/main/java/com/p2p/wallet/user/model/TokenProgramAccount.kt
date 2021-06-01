package com.p2p.wallet.user.model

import android.os.Parcelable
import com.p2p.wallet.amount.toPowerValue
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class TokenProgramAccount(
    val publicKey: String,
    val total: Long,
    val mintAddress: String
) : Parcelable {

    fun getTotal(decimals: Int): BigDecimal =
        BigDecimal(total).divide(decimals.toPowerValue())

    fun getFormattedPrice(swapRate: Double, decimals: Int): BigDecimal =
        getTotal(decimals).times(swapRate.toBigDecimal())
}