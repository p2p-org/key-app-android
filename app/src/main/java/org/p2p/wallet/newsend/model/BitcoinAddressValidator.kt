package org.p2p.wallet.newsend.model

object BitcoinAddressValidator {
    private const val REGEX =
        "(bc|tb)(0([ac-hj-np-z02-9]{39}|[ac-hj-np-z02-9]{59})|" +
            "1[ac-hj-np-z02-9]{8,87})|([13]|[mn2])[a-km-zA-HJ-NP-Z1-9]{25,39}"

    fun isValid(address: String): Boolean = Regex(REGEX).matches(address)
}
