package org.p2p.wallet.auth.ui.phone.countrypicker

import android.content.res.Resources
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.ui.phone.CountryPickerParsingManager
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.auth.ui.phone.model.CountryCodeAdapterItem
import org.p2p.wallet.common.mvp.BasePresenter

class CountryPickerPresenter(resources: Resources) :
    BasePresenter<CountryPickerContract.View>(),
    CountryPickerContract.Presenter {

    private var selectedCountry: CountryCode? = null

    private val countries = CountryPickerParsingManager.readCountriesFromXml(resources).map {
        CountryCodeAdapterItem(country = it, isSelected = false)
    }

    override fun search(name: String) {
        launch {
            if (name.isEmpty()) {
                view?.showCountries(countries)
                return@launch
            }
            val tempCountries = countries
            val searchResult = tempCountries.filter { it.country.name.startsWith(name) }
            view?.showCountries(searchResult)
        }
    }

    override fun load(countryCode: CountryCode?) {
        selectedCountry = countryCode
        view?.showCountries(countries)
    }

    override fun onItemSelected(item: CountryCodeAdapterItem) {
        countries.forEach {
            val country = it.country
            if (country.nameCode.equals(item.country.nameCode, ignoreCase = true)) {
                it.isSelected = true
                selectedCountry = country
            }
        }
        view?.showCountries(countries)
    }

    override fun onCountrySelected() {
        view?.setCountryCode(selectedCountry!!)
    }
}
