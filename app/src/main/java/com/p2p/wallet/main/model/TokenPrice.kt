package com.p2p.wallet.main.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenPrice(
    val tokenSymbol: String,
    val priceInUSD: Double
) : Parcelable