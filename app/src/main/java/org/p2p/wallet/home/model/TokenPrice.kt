package org.p2p.wallet.home.model

import android.os.Parcelable
import org.p2p.core.utils.scaleShort
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class TokenPrice(
    val tokenId: String,
    val price: BigDecimal
) : Parcelable {

    fun getScaledValue(): BigDecimal = price.scaleShort()
}
