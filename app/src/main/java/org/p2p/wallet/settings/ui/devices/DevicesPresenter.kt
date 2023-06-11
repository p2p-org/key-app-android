package org.p2p.wallet.settings.ui.devices

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter

class DevicesPresenter(
    private val deviceCellMapper: DeviceCellMapper
) : BasePresenter<DevicesContract.View>(), DevicesContract.Presenter {

    override fun attach(view: DevicesContract.View) {
        super.attach(view)
        loadInitialData()
    }

    override fun executeDeviceShareChange() {
    }

    private fun loadInitialData() {
        launch {
            // todo: get current and old device share and show them

            val items = deviceCellMapper.toCellModels("Xiaomi 13 Lite", "Redmi 8")
            view?.showCells(items)
        }
    }
}
