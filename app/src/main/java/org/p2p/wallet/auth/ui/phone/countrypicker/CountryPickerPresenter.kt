package org.p2p.wallet.auth.ui.phone.countrypicker

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.ui.phone.CountryCodeInteractor
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.auth.ui.phone.model.CountryCodeAdapterItem
import org.p2p.wallet.common.mvp.BasePresenter

private const val DEFAULT_KEY = ""

class CountryPickerPresenter(
    private val countryCodeInteractor: CountryCodeInteractor
) :
    BasePresenter<CountryPickerContract.View>(),
    CountryPickerContract.Presenter {

    private var selectedCountry: CountryCode? = null
    private val countries = countryCodeInteractor.getCountries().map { CountryCodeAdapterItem(it, false) }
    private var searchText: String = ""
    private val searchTextMap = hashMapOf<String, List<CountryCodeAdapterItem>>().apply {
        put(DEFAULT_KEY, countries)
    }

    override fun search(name: String) {
        searchText = name
        launch {
            if (searchTextMap.containsKey(searchText)) {
                val cachedItems = searchTextMap[searchText].orEmpty()
                view?.showCountries(cachedItems)
            } else {
                val searchResult = countries.filter { it.country.name.startsWith(name, ignoreCase = true) }
                searchTextMap[searchText] = searchResult
                view?.showCountries(searchResult)
            }
        }
    }

    override fun load(countryCode: CountryCode?) {
        selectedCountry = countryCode
        view?.showCountries(countries)
    }

    override fun onItemSelected(item: CountryCodeAdapterItem) {
        val items = searchTextMap[searchText].orEmpty()
        items.forEach {
            val country = it.country
            if (country.nameCode.equals(item.country.nameCode, ignoreCase = true)) {
                it.isSelected = true
                selectedCountry = country
            } else {
                it.isSelected = false
            }
        }
        view?.showCountries(items)
    }

    override fun onCountrySelected() {
        view?.setCountryCode(selectedCountry!!)
    }
}
