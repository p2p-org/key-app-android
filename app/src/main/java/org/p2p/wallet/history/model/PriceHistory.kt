package org.p2p.wallet.history.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PriceHistory(
    val close: Double
) : Parcelable
