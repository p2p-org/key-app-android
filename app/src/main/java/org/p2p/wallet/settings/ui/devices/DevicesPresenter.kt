package org.p2p.wallet.settings.ui.devices

import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
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

private const val MAX_PHONE_NUMBER_TRIES = 5
private const val DEFAULT_BLOCK_TIME_IN_MINUTES = 10

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
            launch {
                view?.setLoadingState(isScreenLoading = true)
                updateDeviceShare()
                view?.setLoadingState(isScreenLoading = false)
                view?.showSuccessDeviceChange()
            }
        } else {
            submitUserPhoneNumber()
        }
    }

    private suspend fun updateDeviceShare() {
        val currentMetadata = metadataInteractor.currentMetadata ?: return
        val newMetadata = currentMetadata.copy(
            deviceShareDeviceName = DeviceInfoHelper.getCurrentDeviceName(),
            deviceNameTimestampSec = DateTimeUtils.getCurrentTimestampInSeconds()
        )
        refreshDeviceShare(newMetadata)
    }

    private suspend fun refreshDeviceShare(newMetadata: GatewayOnboardingMetadata) {
        try {
            restoreWalletInteractor.refreshDeviceShare()
            metadataInteractor.updateMetadata(newMetadata)
        } catch (error: Throwable) {
            view?.showFailDeviceChange()
            Timber.e(error, "Error on refreshDeviceShare")
        }
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
            )?.customSharePhoneNumberE164
                .orEmpty()
                .replace(Regex("[^0-9]"), "")
            val userPhoneNumber = PhoneNumber(phoneNumber)
            onboardingInteractor.temporaryPhoneNumber = userPhoneNumber
            startRestoringCustomShare(userPhoneNumber)
        }
    }

    private suspend fun startRestoringCustomShare(phoneNumber: PhoneNumber) {
        try {
            if (restoreWalletInteractor.getUserEnterPhoneNumberTriesCount() >= MAX_PHONE_NUMBER_TRIES) {
                restoreWalletInteractor.resetUserEnterPhoneNumberTriesCount()
                view?.navigateToAccountBlocked(DEFAULT_BLOCK_TIME_IN_MINUTES.minutes.inWholeSeconds)
                restoreWalletInteractor.resetUserPhoneNumber()
            } else {
                restoreWalletInteractor.startRestoreCustomShare(userPhoneNumber = phoneNumber)
                view?.navigateToSmsInput()
            }
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
        when (val gatewayHandledResult = gatewayServiceErrorHandler.handle(gatewayServiceError)) {
            is GatewayHandledState.CriticalError -> {
                view?.showCommonError()
            }
            is GatewayHandledState.TimerBlockError -> {
                view?.navigateToAccountBlocked(gatewayHandledResult.cooldownTtl)
                restoreWalletInteractor.resetUserPhoneNumber()
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
