package org.p2p.wallet.auth.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountryCode(
    @SerializedName("code_alpha_2")
    val nameCodeAlpha2: String,
    @SerializedName("code_alpha_3")
    val nameCodeAlpha3: String,
    @SerializedName("phone_code")
    val phoneCode: String,
    @SerializedName("name")
    val countryName: String,
    @SerializedName("flag_emoji")
    val flagEmoji: String,
    @SerializedName("phone_mask")
    var mask: String = ""
) : Parcelable {

    val phoneCodeWithPlusSign get() = "+$phoneCode"

    fun getZeroFilledMask(): String {
        return mask.map { symbol -> if (symbol.isDigit()) '0' else ' ' }.joinToString("")
    }
}

data class CountryCodeItem(val country: CountryCode, var isSelected: Boolean)
