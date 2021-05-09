package com.p2p.wallet.token.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PriceHistory(
    val close: Double
) : Parcelable