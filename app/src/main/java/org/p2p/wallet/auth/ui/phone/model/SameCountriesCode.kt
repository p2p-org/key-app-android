package org.p2p.wallet.auth.ui.phone.model

/**
 * Class which contains countries with same countryCode
 */
data class SameCountriesCode(
    val defaultNameCode: String,
    val areaCodeLength: Int,
    val nameCodeToAreaCodesMap: HashMap<String, String>
)
