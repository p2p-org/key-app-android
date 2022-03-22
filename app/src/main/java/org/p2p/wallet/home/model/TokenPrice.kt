package org.p2p.wallet.home.model

import android.os.Parcelable
import org.p2p.wallet.utils.scaleShort
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class TokenPrice(
    val tokenSymbol: String,
    val price: BigDecimal
) : Parcelable {

    fun getScaledValue(): BigDecimal = price.scaleShort()
}
