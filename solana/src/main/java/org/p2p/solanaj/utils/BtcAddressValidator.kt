package org.p2p.solanaj.utils

private const val REGEX = "([13]|bc1)[A-HJ-NP-Za-km-z1-9]{27,34}"

class BtcAddressValidator {

    fun isValid(address: String): Boolean =
        Regex(REGEX).matches(address)
}
