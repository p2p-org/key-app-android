package org.p2p.wallet.striga.presetpicker.interactor

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds

sealed class StrigaPresetDataItem(
    val itemTitle: String
) : Parcelable {

    @Parcelize
    data class Country(val details: CountryCode) :
        StrigaPresetDataItem(details.countryName)

    @Parcelize
    data class SourceOfFunds(val details: StrigaSourceOfFunds) :
        StrigaPresetDataItem(details.sourceName)

    @Parcelize
    data class Occupation(val details: StrigaOccupation) :
        StrigaPresetDataItem(details.occupationName)
}
