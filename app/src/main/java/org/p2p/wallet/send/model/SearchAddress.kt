package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchAddress(
    val address: String,
    val networkType: NetworkType = NetworkType.SOLANA
) : Parcelable
