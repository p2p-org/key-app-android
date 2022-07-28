package org.p2p.wallet.auth.ui.phone.model

import android.util.SparseArray

/**
 * Class which represents groups of SameCountryCodeGroup
 */
data class SameCountriesCodeGroup(
    val groups: SparseArray<SameCountriesCode>
)
