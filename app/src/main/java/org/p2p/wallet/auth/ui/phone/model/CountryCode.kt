package org.p2p.wallet.auth.ui.phone.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountryCode(
    val nameCode: String,
    val phoneCode: String,
    val name: String,
    val flagEmoji: String
) : Parcelable

data class CountryCodeAdapterItem(val country: CountryCode, var isSelected: Boolean)
