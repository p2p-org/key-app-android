package org.p2p.wallet.main.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class NetworkType(val stringValue: String) : Parcelable {
    SOLANA("Solana"),
    BITCOIN("Bitcoin");
}