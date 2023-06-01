package org.p2p.wallet.striga.signup.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StrigaSourceOfFunds(
    val sourceName: String
) : Parcelable
