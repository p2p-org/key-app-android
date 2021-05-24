package com.p2p.wallet.main.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class TokenPrice(
    val tokenSymbol: String,
    val price: Double
) : Parcelable