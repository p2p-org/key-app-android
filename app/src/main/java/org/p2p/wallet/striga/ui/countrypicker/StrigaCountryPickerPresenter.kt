package org.p2p.wallet.striga.ui.countrypicker

import kotlinx.coroutines.launch
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.ui.countrypicker.delegates.StrigaCountryCellModel
import org.p2p.wallet.striga.ui.countrypicker.delegates.StrigaCountryHeaderCellModel
import org.p2p.wallet.utils.emptyString

class StrigaCountryPickerPresenter(
    private val strigaOnboardingInteractor: StrigaOnboardingInteractor,
    private val selectedCountry: Country? = null
) : BasePresenter<StrigaCountryPickerContract.View>(),
    StrigaCountryPickerContract.Presenter {

    private var searchText: String = emptyString()
    private val searchTextMap = hashMapOf<String, List<Country>>()
    private var allCountryCodeItems: List<Country> = listOf()

    override fun search(text: String) {
        searchText = text
        searchByCountryName(text)
    }

    override fun attach(view: StrigaCountryPickerContract.View) {
        super.attach(view)
        launch {
            allCountryCodeItems = strigaOnboardingInteractor.getAllCountries()
            val mappedItems = mapToCellItem(allCountryCodeItems)
            view.showCountries(buildCellList(mappedItems))
        }
    }

    private fun searchByCountryName(countryName: String) {
        launch {
            when (countryName) {
                in searchTextMap -> {
                    val cachedItems = mapToCellItem(searchTextMap[countryName].orEmpty())
                    view?.showCountries(buildCellList(cachedItems))
                }
                else -> {
                    val searchResult = allCountryCodeItems
                        .filter {
                            it.name.contains(countryName, ignoreCase = true)
                        }
                    searchTextMap[countryName] = searchResult
                    val newCacheItems = mapToCellItem(searchTextMap[countryName].orEmpty())
                    view?.showCountries(buildCellList(newCacheItems))
                }
            }
        }
    }

    private fun mapToCellItem(items: List<Country>): List<AnyCellItem> {
        return items.map { item -> StrigaCountryCellModel(item) }
    }

    private fun buildCellList(items: List<AnyCellItem>): List<AnyCellItem> = buildList {
        if (selectedCountry != null) {
            add(StrigaCountryHeaderCellModel(R.string.striga_chosen_country))
            add(StrigaCountryCellModel(selectedCountry))
        }
        add(StrigaCountryHeaderCellModel(R.string.striga_all_countries))
        addAll(items)
    }
}
