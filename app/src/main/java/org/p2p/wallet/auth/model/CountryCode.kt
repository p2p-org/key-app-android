package org.p2p.wallet.auth.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountryCode(
    val nameCodeAlpha2: String,
    val nameCodeAlpha3: String,
    val phoneCode: String,
    val countryName: String,
    val flagEmoji: String,
    var mask: String = ""
) : Parcelable {

    fun getZeroFilledMask(): String {
        return mask.map { symbol -> if (symbol.isDigit()) '0' else ' ' }.joinToString("")
    }
}

data class CountryCodeItem(val country: CountryCode, var isSelected: Boolean)
