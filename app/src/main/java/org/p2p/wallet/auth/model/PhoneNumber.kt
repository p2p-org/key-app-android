package org.p2p.wallet.auth.model

data class PhoneNumber(val value: String) {

    object PhoneNumberFormatter {

        fun getPhoneNumberE164Formatted(phoneNumber: PhoneNumber): String {
            val trimmedValue = phoneNumber.value.replace(" ", "")
            return "+$trimmedValue"
        }
    }
}

fun PhoneNumber.e164Formatted(): String {
    return PhoneNumber.PhoneNumberFormatter.getPhoneNumberE164Formatted(this)
}

fun PhoneNumber.equals(phoneNumber: PhoneNumber?): Boolean {
    return this.value == phoneNumber?.value
}
