package org.p2p.wallet.auth.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class which represent phone number format
 * [formattedValue] contains countryCode + phoneNumber(formatted by PhoneNumberUtil) without trim
 */
@Parcelize
data class PhoneNumber(val formattedValue: String) : Parcelable {

    object PhoneNumberFormatter {

        fun getPhoneNumberE164Formatted(phoneNumber: PhoneNumber): String {
            val trimmedValue = phoneNumber.formattedValue.replace(" ", "")
            return "+$trimmedValue"
        }
    }

    fun e164Formatted(): String {
        return PhoneNumberFormatter.getPhoneNumberE164Formatted(this)
    }
}
