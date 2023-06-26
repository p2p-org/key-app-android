package org.p2p.wallet.striga.wallet.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StrigaBlockchainNetworkInfo(
    val name: String,
    val contractAddress: String?,
    val type: String?
) : Parcelable
