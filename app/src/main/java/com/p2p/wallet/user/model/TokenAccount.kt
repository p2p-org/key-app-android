package com.p2p.wallet.user.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.pow

@Parcelize
data class TokenAccount(
    val depositAddress: String,
    val amount: Long,
    val mintAddress: String
) : Parcelable {

    fun getUSPrice(decimals: Int, isUS: Boolean): Double =
        if (isUS) amount.toDouble() / (10.0.pow(decimals)) else 0.0

    fun getAmount(decimals: Int): Double =
        amount.toDouble() / (10.0.pow(decimals))
}