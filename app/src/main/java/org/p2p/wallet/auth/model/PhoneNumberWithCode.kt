package org.p2p.wallet.auth.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.uikit.utils.SimpleMaskFormatter

/**
 * @param phoneCode - country of phone number
 * @param phoneNumberNational - national phone number (without phone code)
 */
@Parcelize
data class PhoneNumberWithCode(
    val phoneCode: CountryCode,
    val phoneNumberNational: String,
) : Parcelable {

    val phoneCodeWithPlusSign: String
        get() = "+${phoneCode.phoneCode}"

    val formatterPhoneNumber: String
        get() = "${phoneCodeWithPlusSign}$phoneNumberNational"

    val formattedPhoneNumberByMask: String
        get() = phoneCode.mask.replace(Regex("\\d"), "#")
            .let { SimpleMaskFormatter(it).format(phoneNumberNational) }
            .let { "${phoneCodeWithPlusSign}$it" }
}
