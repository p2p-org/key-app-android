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

    fun e164Formatted(): String {
        return PhoneNumberFormatter.getPhoneNumberE164Formatted(this)
    }
}
