package com.p2p.wowlet.entities.local

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WalletItem(
    val tokenSymbol: String = "",
    val depositAddress: String = "",
    val decimals: Int = 0,
    val mintAddress: String = "",
    var tokenName: String = "",
    val icon: String = "",
    var price: Double = 0.0,
    var amount: Double = 0.0,
    var walletBinds: Double = 0.0
): Parcelable