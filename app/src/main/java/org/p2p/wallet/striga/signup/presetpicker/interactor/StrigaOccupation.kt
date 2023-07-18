package org.p2p.wallet.striga.signup.presetpicker.interactor

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StrigaOccupation(
    val occupationName: String,
    val emoji: String
) : Parcelable
