package org.p2p.wallet.striga.presetpicker.presenter

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataPickerContract
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataSearcher
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataInteractor
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.presetpicker.mapper.StrigaItemCellMapper

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
                .filter { it.getName() != selectedPresetDataItem.getName() }
            view?.showItems(strigaElementCellMapper.buildCellModels(allItems, selectedPresetDataItem))
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
            strigaElementCellMapper.buildCellModels(
                items = searchResult,
                selectedItem = selectedPresetDataItem.takeIf { it in searchResult }
            )
        }
        view?.showItems(resultCellModels)
    }

    override fun onPresetDataSelected(item: StrigaPresetDataItem) {
        view?.closeWithResult(item)
    }
}
