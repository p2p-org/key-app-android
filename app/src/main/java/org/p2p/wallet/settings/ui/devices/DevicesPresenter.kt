package org.p2p.wallet.settings.ui.devices

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.settings.DeviceInfoHelper
import org.p2p.wallet.utils.DateTimeUtils

class DevicesPresenter(
    private val secureStorage: SecureStorageContract,
    private val deviceCellMapper: DeviceCellMapper,
    private val metadataInteractor: MetadataInteractor
) : BasePresenter<DevicesContract.View>(), DevicesContract.Presenter {

    override fun attach(view: DevicesContract.View) {
        super.attach(view)
        loadInitialData()
    }

    override fun executeDeviceShareChange() {
        // TODO PWN-8351 add start point of change device share flow
        val tKeyIsAlive = true // todo check on live tKey instance 15 min!
        if (tKeyIsAlive) {
            // TODO update with real logic
            launch {
                view?.setLoadingState(isScreenLoading = true)
                updateMetadata()
                delay(5000)
                view?.setLoadingState(isScreenLoading = false)
                view?.showSuccessDeviceChange()
            }
        } else {
            // TODO open OTP
        }
    }

    private suspend fun updateMetadata() {
        val currentMetadata = metadataInteractor.currentMetadata ?: return
        val newMetadata = currentMetadata.copy(
            deviceShareDeviceName = DeviceInfoHelper.getCurrentDeviceName(),
            deviceNameTimestampSec = DateTimeUtils.getCurrentTimestampInSeconds()
        )
        metadataInteractor.updateMetadata(newMetadata)
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
