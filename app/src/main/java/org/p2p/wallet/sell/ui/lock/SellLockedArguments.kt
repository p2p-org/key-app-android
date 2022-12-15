package org.p2p.wallet.sell.ui.lock

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize

@Parcelize
class SellLockedArguments(
    val solAmount: BigDecimal,
    val amountInUsd: String,
    val moonpayAddress: String
) : Parcelable
