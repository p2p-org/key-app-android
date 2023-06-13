package org.p2p.wallet.settings.ui.devices

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.settings.DeviceInfoHelper

class DevicesPresenter(
    private val secureStorage: SecureStorageContract,
    private val deviceCellMapper: DeviceCellMapper,
) : BasePresenter<DevicesContract.View>(), DevicesContract.Presenter {

    override fun attach(view: DevicesContract.View) {
        super.attach(view)
        loadInitialData()
    }

    override fun executeDeviceShareChange() {
        // TODO PWN-8351 add start point of change device share flow
    }

    private fun loadInitialData() {
        launch {
            val oldDeviceShareName = secureStorage.getObject(
                SecureStorageContract.Key.KEY_ONBOARDING_METADATA,
                GatewayOnboardingMetadata::class
            )?.deviceShareDeviceName.orEmpty()
            val items = deviceCellMapper.toCellModels(
                DeviceInfoHelper.getCurrentDeviceName(),
                oldDeviceShareName
            )
            view?.showCells(items)
        }
    }
}
