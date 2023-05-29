package org.p2p.wallet.striga.ui.countrypicker

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.model.StrigaCountryPickerItem
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor

class StrigaCountryPickerPresenter(
    private val strigaOnboardingInteractor: StrigaOnboardingInteractor,
    private val selectedCountry: Country? = null
) : BasePresenter<StrigaCountryPickerContract.View>(),
    StrigaCountryPickerContract.Presenter {

    override fun attach(view: StrigaCountryPickerContract.View) {
        super.attach(view)
        launch {
            view.showCountries(buildUiList())
        }
    }

    private suspend fun buildUiList(): List<StrigaCountryPickerItem> {
        val allCountries = strigaOnboardingInteractor.getAllCountries()
        return buildList {
            if (selectedCountry != null) {
                add(StrigaCountryPickerItem.HeaderItem(R.string.striga_chosen_country))
                add(StrigaCountryPickerItem.CountryItem(selectedCountry))
            }
            add(StrigaCountryPickerItem.HeaderItem(R.string.striga_all_countries))
            addAll(allCountries.map { StrigaCountryPickerItem.CountryItem(it) })
        }
    }
}
