package org.p2p.wallet.newsend.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class NetworkType(val stringValue: String) : Parcelable {
    SOLANA("Solana"),
    ETHEREUM("Ethereum");
}
