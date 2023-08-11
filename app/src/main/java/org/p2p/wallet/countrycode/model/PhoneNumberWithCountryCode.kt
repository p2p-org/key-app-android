package org.p2p.wallet.countrycode.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.uikit.utils.SimpleMaskFormatter

/**
 * @param countryCode - country of phone number
 * @param phoneNumberNational - national phone number (without phone code)
 * @param phoneCode - phone code digits without plus sign (e.g. 995 for Georgia, 1 for USA, et cetera)
 * @param mask - mask for phone number (see libphonenumber)
 */
@Parcelize
data class PhoneNumberWithCountryCode(
    val countryCode: ExternalCountryCode,
    val phoneCode: String,
    val phoneNumberNational: String,
    val mask: String,
) : Parcelable {

    val phoneCodeWithPlusSign: String
        get() = "+$phoneCode"

    val formatterPhoneNumber: String
        get() = "${phoneCodeWithPlusSign}$phoneNumberNational"

    val formattedPhoneNumberByMask: String
        get() = mask.replace(Regex("\\d"), "#")
            .let { SimpleMaskFormatter(it).format(phoneNumberNational) }
            .let { "${phoneCodeWithPlusSign}$it" }
}
