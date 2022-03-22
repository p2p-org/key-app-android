package org.p2p.wallet.user.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenData(
    val mintAddress: String,
    val name: String,
    val symbol: String,
    val iconUrl: String?,
    val decimals: Int,
    val isWrapped: Boolean,
    val serumV3Usdc: String?,
    val serumV3Usdt: String?
) : Parcelable
