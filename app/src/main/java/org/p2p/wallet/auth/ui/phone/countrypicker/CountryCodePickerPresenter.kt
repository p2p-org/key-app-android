package org.p2p.wallet.auth.ui.phone.countrypicker

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.CountryCodeItem
import org.p2p.wallet.auth.ui.phone.CountryCodeInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString

private const val DEFAULT_KEY = ""
private const val PLUS_SIGN = '+'

class CountryCodePickerPresenter(
    private val countryCodeInteractor: CountryCodeInteractor
) :
    BasePresenter<CountryCodePickerContract.View>(),
    CountryCodePickerContract.Presenter {

    private var selectedCountryCode: CountryCode? = null
    private var searchText: String = emptyString()
    private val searchTextMap = hashMapOf<String, List<CountryCodeItem>>()
    private var allCountryCodeItems: List<CountryCodeItem> = listOf()

    override fun search(text: String) {
        searchText = text
        if (text.all { it.isDigit() || it == PLUS_SIGN }) searchByCountryCode(text) else searchByCountryName(text)
    }

    private fun searchByCountryName(countryName: String) {
        launch {
            if (countryName in searchTextMap) {
                val cachedItems = searchTextMap[countryName].orEmpty()
                view?.showCountries(cachedItems)
            } else {
                val searchResult =
                    allCountryCodeItems.filter { it.country.name.contains(countryName, ignoreCase = true) }
                searchTextMap[countryName] = searchResult
                view?.showCountries(searchResult)
            }
        }
    }

    private fun searchByCountryCode(countryCode: String) {
        launch {
            if (countryCode in searchTextMap) {
                val cachedItems = searchTextMap[countryCode].orEmpty()
                view?.showCountries(cachedItems)
            } else {
                val codeWithoutPlus = countryCode.filterNot { it == PLUS_SIGN }
                val searchResult = allCountryCodeItems.filter {
                    it.country.phoneCode.startsWith(codeWithoutPlus)
                }
                searchTextMap[countryCode] = searchResult
                view?.showCountries(searchResult)
            }
        }
    }

    override fun load(countryCode: CountryCode?) {
        launch {
            allCountryCodeItems = countryCodeInteractor.getCountries()
                .map { CountryCodeItem(it, isSelected = it.nameCode == countryCode?.nameCode) }
                .sortedBy { !it.isSelected }
            selectedCountryCode = countryCode
            searchTextMap[DEFAULT_KEY] = allCountryCodeItems
            view?.showCountries(allCountryCodeItems)
        }
    }
}
