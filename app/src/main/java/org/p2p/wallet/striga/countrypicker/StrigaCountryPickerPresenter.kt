package org.p2p.wallet.striga.countrypicker

import androidx.annotation.StringRes
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.utils.emptyString

class StrigaCountryPickerPresenter(
    private val strigaOnboardingInteractor: StrigaOnboardingInteractor,
    private val selectedCountry: Country? = null
) : BasePresenter<StrigaCountryPickerContract.View>(),
    StrigaCountryPickerContract.Presenter {

    private var searchText: String = emptyString()
    private val searchTextMap = hashMapOf<String, List<Country>>()
    private var allCountryItems: List<Country> = listOf()

    override fun search(text: String) {
        searchText = text
        searchByCountryName(text)
    }

    override fun attach(view: StrigaCountryPickerContract.View) {
        super.attach(view)
        launch {
            allCountryItems = strigaOnboardingInteractor.getAllCountries()
                .filter { it.name != selectedCountry?.name }
            view.showCountries(buildCellList(allCountryItems.map(::mapToCellItem)))
        }
    }

    private fun searchByCountryName(countryName: String) {
        launch {
            when (countryName) {
                in searchTextMap -> {
                    val cachedItems = searchTextMap[countryName].orEmpty().map(::mapToCellItem)
                    view?.showCountries(buildCellList(cachedItems))
                }
                else -> {
                    val searchResult = allCountryItems.filter {
                        it.name.contains(countryName, ignoreCase = true)
                    }
                    searchTextMap[countryName] = searchResult
                    val newCacheItems = searchTextMap[countryName].orEmpty().map(::mapToCellItem)
                    view?.showCountries(buildCellList(newCacheItems))
                }
            }
        }
    }

    private fun mapToCellItem(item: Country): AnyCellItem {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer.Companion.invoke(item.name),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                textColor = R.color.text_night
            ),
            icon = IconWrapperCellModel.SingleEmoji(emoji = item.flagEmoji)
        )
        return FinanceBlockCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = item
        )
    }

    private fun buildCellList(countryItems: List<AnyCellItem>): List<AnyCellItem> = buildList {
        if (selectedCountry != null && countryItems.isNotEmpty()) {
            this += buildHeaderCellItem(R.string.striga_chosen_country)
            this += mapToCellItem(selectedCountry)
        }
        if (countryItems.isNotEmpty()) {
            this += buildHeaderCellItem(R.string.striga_all_countries)
            this += countryItems
        }
    }

    private fun buildHeaderCellItem(@StringRes titleRes: Int): AnyCellItem {
        return SectionHeaderCellModel(
            sectionTitle = TextContainer(titleRes),
            isShevronVisible = false,
            textColor = R.color.text_mountain,
            backgroundColor = R.color.bg_smoke,
            textAppearance = R.style.UiKit_TextAppearance_Regular_Caps
        )
    }
}
