package org.p2p.wallet.settings.ui.devices

import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.gateway.repository.model.PushServiceError
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.repository.GatewayServiceErrorHandler
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.settings.DeviceInfoHelper
import org.p2p.wallet.utils.DateTimeUtils

class DevicesPresenter(
    private val secureStorage: SecureStorageContract,
    private val deviceCellMapper: DeviceCellMapper,
    private val metadataInteractor: MetadataInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val gatewayServiceErrorHandler: GatewayServiceErrorHandler,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
) : BasePresenter<DevicesContract.View>(), DevicesContract.Presenter {

    override fun attach(view: DevicesContract.View) {
        super.attach(view)
        loadInitialData()
    }

    override fun executeDeviceShareChange() {
        val isTorusKeyValid = restoreFlowDataLocalRepository.isTorusKeyValid()
        if (isTorusKeyValid) {
            // TODO update with real logic
            launch {
                view?.setLoadingState(isScreenLoading = true)
                updateMetadata()
                delay(5000)
                view?.setLoadingState(isScreenLoading = false)
                view?.showSuccessDeviceChange()
            }
        } else {
            submitUserPhoneNumber()
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

    private fun submitUserPhoneNumber() {
        launch {
            restoreWalletInteractor.generateRestoreUserKeyPair()
            val phoneNumber = secureStorage.getObject(
                SecureStorageContract.Key.KEY_ONBOARDING_METADATA,
                GatewayOnboardingMetadata::class
            )?.customSharePhoneNumberE164.orEmpty().replace(Regex("[^0-9]"), "")
            val userPhoneNumber = PhoneNumber(phoneNumber)
            onboardingInteractor.temporaryPhoneNumber = userPhoneNumber
            startRestoringCustomShare(userPhoneNumber)
        }
    }

    private suspend fun startRestoringCustomShare(phoneNumber: PhoneNumber) {
        try {
            restoreWalletInteractor.startRestoreCustomShare(userPhoneNumber = phoneNumber)
            view?.navigateToSmsInput()
        } catch (error: Throwable) {
            if (error is PushServiceError) {
                handleGatewayServiceError(error)
            } else {
                Timber.e(error, "Phone number submission failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            restoreWalletInteractor.setIsRestoreWalletRequestSent(isSent = false)
        }
    }

    private fun handleGatewayServiceError(gatewayServiceError: PushServiceError) {
        // TODO PWN-8827 fix error handling!
        when (val gatewayHandledResult = gatewayServiceErrorHandler.handle(gatewayServiceError)) {
            is GatewayHandledState.CriticalError -> {
                view?.showUiKitSnackBar(gatewayHandledResult.errorCode.toString())
            }
            is GatewayHandledState.TimerBlockError -> {
                view?.showUiKitSnackBar(gatewayHandledResult.error.name)
            }
            is GatewayHandledState.TitleSubtitleError -> {
                view?.showUiKitSnackBar(gatewayHandledResult.title)
            }
            is GatewayHandledState.ToastError -> {
                view?.showUiKitSnackBar(gatewayHandledResult.message)
            }
            else -> {
                // Do nothing
            }
        }
    }
}
