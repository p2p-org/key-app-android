package org.p2p.wallet.auth.repository

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Country(
    val name: String,
    val flagEmoji: String,
    val code: String,
) : Parcelable {
    val nameLowercase: String
        get() = name.lowercase()
}
