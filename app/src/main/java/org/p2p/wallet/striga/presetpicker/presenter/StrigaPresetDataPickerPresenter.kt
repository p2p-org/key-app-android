package org.p2p.wallet.striga.presetpicker.presenter

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataPickerContract
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataToPick
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataInteractor
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.presetpicker.mapper.StrigaItemCellMapper
import org.p2p.wallet.striga.ui.countrypicker.StrigaPresetDataSearcher

class StrigaPresetDataPickerPresenter(
    private val presetDataToPick: StrigaPresetDataToPick,
    private val strigaElementCellMapper: StrigaItemCellMapper,
    private val dataSearcher: StrigaPresetDataSearcher,
    private val strigaPresetDataInteractor: StrigaPresetDataInteractor,
    dispatchers: CoroutineDispatchers,
) : BasePresenter<StrigaPresetDataPickerContract.View>(dispatchers.ui),
    StrigaPresetDataPickerContract.Presenter {

    private var allItems: List<StrigaPresetDataItem> = listOf()
    private var selectedItem: StrigaPresetDataItem? = null

    override fun attach(view: StrigaPresetDataPickerContract.View) {
        super.attach(view)
        loadElements()
    }

    private fun loadElements() {
        launch {
            selectedItem = strigaPresetDataInteractor.getSelectedPresetDataItem(presetDataToPick)
            allItems = strigaPresetDataInteractor.getPresetData(presetDataToPick)

            view?.showItems(strigaElementCellMapper.buildCellModels(allItems, selectedItem))
        }
    }

    override fun search(text: String) {
        val resultCellModels = if (text.isBlank()) {
            strigaElementCellMapper.buildCellModels(
                items = allItems,
                selectedItem = selectedItem
            )
        } else {
            val searchResult = dataSearcher.search(text, allItems)
            strigaElementCellMapper.buildCellModels(
                items = searchResult,
                selectedItem = selectedItem?.takeIf { it in searchResult }
            )
        }
        view?.showItems(resultCellModels)
    }

    override fun onPresetDataSelected(item: StrigaPresetDataItem) {
        launch {
            strigaPresetDataInteractor.saveSelectedPresetData(presetDataToPick, item)
                .onSuccess {
                    Timber.i("$presetDataToPick saved")
                    view?.close()
                }
        }
    }
}
