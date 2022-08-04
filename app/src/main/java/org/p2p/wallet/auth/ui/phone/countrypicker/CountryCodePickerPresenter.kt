package org.p2p.wallet.auth.ui.phone.countrypicker

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.ui.phone.CountryCodeInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.CountryCodeItem
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString

private const val DEFAULT_KEY = ""

class CountryCodePickerPresenter(
    private val countryCodeInteractor: CountryCodeInteractor
) :
    BasePresenter<CountryCodePickerContract.View>(),
    CountryCodePickerContract.Presenter {

    private var selectedCountryCode: CountryCode? = null
    private var searchTextByCountryCode: String = emptyString()
    private val searchTextMap = hashMapOf<String, List<CountryCodeItem>>()
    private var allCountryCodeItems: List<CountryCodeItem> = listOf()

    override fun searchByCountryName(contryName: String) {
        searchTextByCountryCode = contryName
        launch {
            if (searchTextByCountryCode in searchTextMap) {
                val cachedItems = searchTextMap[searchTextByCountryCode].orEmpty()
                view?.showCountries(cachedItems)
            } else {
                val searchResult =
                    allCountryCodeItems.filter { it.country.name.startsWith(contryName, ignoreCase = true) }
                searchTextMap[searchTextByCountryCode] = searchResult
                view?.showCountries(searchResult)
            }
        }
    }

    override fun load(countryCode: CountryCode?) {
        launch {
            allCountryCodeItems = countryCodeInteractor.getCountries().map { CountryCodeItem(it, false) }
            selectedCountryCode = countryCode
            searchTextMap[DEFAULT_KEY] = allCountryCodeItems
            view?.showCountries(allCountryCodeItems)
        }
    }

    // TODO refactor this method
    override fun onItemSelected(item: CountryCodeItem) {
        val countryCodesBySearchText = searchTextMap[searchTextByCountryCode].orEmpty()
        countryCodesBySearchText.forEach {
            val country = it.country
            if (country.nameCode.equals(item.country.nameCode, ignoreCase = true)) {
                it.isSelected = true
                selectedCountryCode = country
            } else {
                it.isSelected = false
            }
        }
        view?.showCountries(countryCodesBySearchText)
    }

    override fun onCountryCodeSelected() {
        selectedCountryCode?.let { view?.setCountryCode(it) }
    }
}
