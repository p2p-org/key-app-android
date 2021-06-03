package com.p2p.wallet.main.model

import android.os.Parcelable
import com.p2p.wallet.amount.scaleShort
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class TokenPrice(
    val tokenSymbol: String,
    val price: BigDecimal
) : Parcelable {

    fun getFormattedPrice() : BigDecimal = price.scaleShort()
}