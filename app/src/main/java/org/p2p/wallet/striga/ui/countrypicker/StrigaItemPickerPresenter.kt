package org.p2p.wallet.striga.ui.countrypicker

import androidx.annotation.StringRes
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.signup.model.StrigaPickerItem
import org.p2p.wallet.utils.emptyString

class StrigaItemPickerPresenter(
    private val strigaOnboardingInteractor: StrigaOnboardingInteractor,
    private val selectedItem: StrigaPickerItem,
    private val strigaElementCellMapper: StrigaItemCellMapper
) : BasePresenter<StrigaItemPickerContact.View>(),
    StrigaItemPickerContact.Presenter {

    private var searchText: String = emptyString()
    private val searchTextMap = hashMapOf<String, List<FinanceBlockCellModel>>()
    private var allItems: List<FinanceBlockCellModel> = listOf()

    override fun search(text: String) {
        searchText = text
        searchByName(text)
    }

    override fun attach(view: StrigaItemPickerContact.View) {
        super.attach(view)
        loadElements()
        view.updateSearchTitle(strigaElementCellMapper.getSearchTitleResId(selectedItem))
    }

    private fun searchByName(searchedName: String) {
        launch {
            when (searchedName) {
                in searchTextMap -> {
                    val cachedItems = searchTextMap[searchedName].orEmpty()
                    view?.showItems(buildCellList(cachedItems))
                }
                else -> {
                    val searchResult = allItems.filter {
                        val pickerItem = it.payload as StrigaPickerItem
                        pickerItem.getTitle().orEmpty().contains(searchedName, ignoreCase = true)
                    }
                    searchTextMap[searchedName] = searchResult
                    val newCacheItems = searchTextMap[searchedName].orEmpty()
                    view?.showItems(buildCellList(newCacheItems))
                }
            }
        }
    }

    private fun buildCellList(items: List<AnyCellItem>): List<AnyCellItem> = buildList {
        if (selectedItem.getTitle() != null && items.isNotEmpty()) {
            this += buildHeaderCellItem(strigaElementCellMapper.getSelectedItemHeaderTitle(selectedItem))
            this += strigaElementCellMapper.mapItemToCellItem(selectedItem)
        }
        if (items.isNotEmpty()) {
            this += buildHeaderCellItem(strigaElementCellMapper.getAllItemHeaderTitle(selectedItem))
            addAll(items)
        }
    }.filterNotNull()

    private fun buildHeaderCellItem(@StringRes titleRes: Int): AnyCellItem {
        return SectionHeaderCellModel(
            sectionTitle = TextContainer(titleRes),
            isShevronVisible = false,
            textColor = R.color.text_mountain,
            backgroundColor = R.color.bg_smoke,
            textAppearance = R.style.UiKit_TextAppearance_Regular_Caps
        )
    }

    private fun loadElements() {
        launch {
            allItems = when (selectedItem) {
                is StrigaPickerItem.CountryItem -> {
                    strigaOnboardingInteractor.getAllCountries()
                        .filter { it.name != selectedItem.selectedItem?.name }
                        .map { strigaElementCellMapper.mapItemToCellItem(it) }
                }
                is StrigaPickerItem.FundsItem -> {
                    strigaOnboardingInteractor.getSourceOfFundsList()
                        .filter { it.sourceName != selectedItem.selectedItem?.sourceName }
                        .map { strigaElementCellMapper.mapItemToCellItem(it) }
                }
                is StrigaPickerItem.OccupationItem -> {
                    strigaOnboardingInteractor.getOccupationValuesList()
                        .filter { it.occupationName != selectedItem.selectedItem?.occupationName }
                        .map { strigaElementCellMapper.mapItemToCellItem(it) }
                }
            }
            view?.showItems(buildCellList(allItems))
        }
    }
}
