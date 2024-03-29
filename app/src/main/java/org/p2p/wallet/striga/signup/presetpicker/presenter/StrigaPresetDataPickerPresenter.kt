package org.p2p.wallet.striga.signup.presetpicker.presenter

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.signup.presetpicker.StrigaPresetDataPickerContract
import org.p2p.wallet.striga.signup.presetpicker.StrigaPresetDataSearcher
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataInteractor
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.signup.presetpicker.mapper.StrigaItemCellMapper

class StrigaPresetDataPickerPresenter(
    private val strigaElementCellMapper: StrigaItemCellMapper,
    private val dataSearcher: StrigaPresetDataSearcher,
    private val strigaPresetDataInteractor: StrigaPresetDataInteractor,
    private var selectedPresetDataItem: StrigaPresetDataItem,
    dispatchers: CoroutineDispatchers,
) : BasePresenter<StrigaPresetDataPickerContract.View>(dispatchers.ui),
    StrigaPresetDataPickerContract.Presenter {

    private var allItems: List<StrigaPresetDataItem> = listOf()

    override fun attach(view: StrigaPresetDataPickerContract.View) {
        super.attach(view)
        loadElements()
    }

    private fun loadElements() {
        launch {
            allItems = strigaPresetDataInteractor.getPresetData(selectedPresetDataItem)

            view?.showItems(
                items = strigaElementCellMapper.buildCellModels(
                    items = allItems.filter { it.getName() != selectedPresetDataItem.getName() },
                    selectedItem = selectedPresetDataItem
                )
            )
        }
    }

    override fun search(text: String) {
        val resultCellModels = if (text.isBlank()) {
            strigaElementCellMapper.buildCellModels(
                items = allItems,
                selectedItem = selectedPresetDataItem
            )
        } else {
            val searchResult = dataSearcher.search(text, allItems)
            strigaElementCellMapper.buildSearchCellModels(
                items = searchResult,
                selectedItem = selectedPresetDataItem
            )
        }
        view?.showItems(resultCellModels)
    }

    override fun onPresetDataSelected(item: StrigaPresetDataItem) {
        view?.closeWithResult(item)
    }
}
