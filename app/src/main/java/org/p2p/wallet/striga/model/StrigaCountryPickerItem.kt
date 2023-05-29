package org.p2p.wallet.striga.model

import androidx.annotation.StringRes
import org.p2p.wallet.auth.repository.Country

sealed class StrigaCountryPickerItem {
    data class HeaderItem(@StringRes val tileResId: Int) : StrigaCountryPickerItem()
    data class CountryItem(val country: Country) : StrigaCountryPickerItem()
}
