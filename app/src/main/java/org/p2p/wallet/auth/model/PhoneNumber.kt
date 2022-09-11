package org.p2p.wallet.auth.model

/**
 * Data class which represent phone number format
 * [formattedValue] contains countryCode + phoneNumber(formatted by PhoneNumberUtil) without trim
 */
data class PhoneNumber(val formattedValue: String) {

    object PhoneNumberFormatter {

        fun getPhoneNumberE164Formatted(phoneNumber: PhoneNumber): String {
            val trimmedValue = phoneNumber.formattedValue.replace(" ", "")
            return "+$trimmedValue"
        }
    }
}

fun PhoneNumber.e164Formatted(): String {
    return PhoneNumber.PhoneNumberFormatter.getPhoneNumberE164Formatted(this)
}

fun PhoneNumber.equals(phoneNumber: PhoneNumber?): Boolean {
    return this.formattedValue == phoneNumber?.formattedValue
}
