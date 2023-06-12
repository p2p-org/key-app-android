package org.p2p.wallet.auth.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
}
