package org.p2p.wallet.auth.model


data class PhoneMask(
    val countryCodeAlpha2: String,
    val phoneCode: String,
    val mask: String,
) {
    val phoneCodeWithSign = "+$phoneCode"
}
