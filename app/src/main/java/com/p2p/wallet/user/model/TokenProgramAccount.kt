package com.p2p.wallet.user.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import kotlin.math.pow

@Parcelize
data class TokenProgramAccount(
    val publicKey: String,
    val total: Long,
    val mintAddress: String
) : Parcelable {

    fun getAmount(decimals: Int): BigDecimal =
        BigDecimal(total).divide(BigDecimal(10.0.pow(decimals)))

    fun getFormattedPrice(swapRate: BigDecimal, decimals: Int): BigDecimal =
        getAmount(decimals).times(swapRate)
}