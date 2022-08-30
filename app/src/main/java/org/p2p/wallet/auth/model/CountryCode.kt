package org.p2p.wallet.auth.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.utils.emptyString

@Parcelize
data class CountryCode(
    val nameCode: String,
    val phoneCode: String,
    val name: String,
    val flagEmoji: String,
    var mask: String = ""
) : Parcelable {

    fun getZeroFilledMask(): String {
        var zeroFilledMask = emptyString()
        mask.forEach { symbol ->
            zeroFilledMask += if (symbol.isDigit()) {
                "0"
            } else {
                " "
            }
        }
        return zeroFilledMask
    }
}

data class CountryCodeItem(val country: CountryCode, var isSelected: Boolean)
