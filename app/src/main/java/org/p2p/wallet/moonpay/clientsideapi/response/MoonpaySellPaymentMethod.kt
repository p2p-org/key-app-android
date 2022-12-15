package org.p2p.wallet.moonpay.clientsideapi.response

enum class MoonpaySellPaymentMethod(val stringValue: String) {
    ACH_BANK_TRANFER("ach_bank_transfer"),
    SEPA_BANK_TRANFER("sepa_bank_transfer"),
    UNKNOWN_BANK("unknown");

    companion object {
        fun fromStringValue(value: String): MoonpaySellPaymentMethod =
            values().find { it.stringValue == value } ?: UNKNOWN_BANK
    }
}
