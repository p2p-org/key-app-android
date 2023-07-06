package org.p2p.wallet.striga.presetpicker.interactor

import androidx.annotation.StringRes
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds

sealed class StrigaPresetDataItem() : Parcelable {

    abstract val toolbarTitleId: Int
    abstract val searchTitleId: Int

    @Parcelize
    data class Country(
        val details: CountryCode?
    ) : StrigaPresetDataItem() {
        @StringRes
        override val toolbarTitleId: Int = R.string.striga_select_your_country

        @StringRes
        override val searchTitleId: Int = R.string.striga_preset_data_hint_country
    }

    @Parcelize
    data class SourceOfFunds(
        val details: StrigaSourceOfFunds?
    ) : StrigaPresetDataItem() {
        @StringRes
        override val toolbarTitleId: Int = R.string.striga_select_your_source

        @StringRes
        override val searchTitleId: Int = R.string.striga_preset_data_hint_source
    }

    @Parcelize
    data class Occupation(
        val details: StrigaOccupation?
    ) : StrigaPresetDataItem() {
        @StringRes
        override val toolbarTitleId: Int = R.string.striga_select_your_occupation

        @StringRes
        override val searchTitleId: Int = R.string.striga_preset_data_hint_occupation
    }

    fun getName(): String {
        return when (this) {
            is Country -> details?.countryName
            is Occupation -> details?.occupationName
            is SourceOfFunds -> details?.sourceName
        }.orEmpty()
    }
}
