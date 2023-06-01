package org.p2p.wallet.striga.signup.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.auth.repository.Country

sealed class StrigaPickerItem : Parcelable {
    @Parcelize
    data class OccupationItem(val selectedItem: StrigaOccupation? = null) : StrigaPickerItem()
    @Parcelize
    data class FundsItem(val selectedItem: StrigaSourceOfFunds? = null) : StrigaPickerItem()
    @Parcelize
    data class CountryItem(val selectedItem: Country? = null) : StrigaPickerItem()

    fun getTitle(): String? = when (this) {
        is CountryItem -> selectedItem?.name
        is FundsItem -> selectedItem?.sourceName
        is OccupationItem -> selectedItem?.occupationName
    }
}
