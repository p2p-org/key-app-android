package org.p2p.wallet.newsend.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Deprecated("Redundant class, will be removed")
@Parcelize
data class AddressState(
    val address: String
) : Parcelable
