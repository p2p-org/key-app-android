package org.p2p.wallet.settings.ui.mail

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreError
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.model.RestoreSuccessState
import org.p2p.wallet.auth.repository.RestoreUserResultHandler
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.DeviceInfoHelper
import org.p2p.wallet.utils.DateTimeUtils

class SettingsEmailConfirmPresenter(
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val restoreUserResultHandler: RestoreUserResultHandler,
    private val metadataInteractor: MetadataInteractor,
) : BasePresenter<SettingsEmailConfirmContract.View>(), SettingsEmailConfirmContract.Presenter {

    override fun setGoogleIdToken(userId: String, idToken: String) {
        launch {
            view?.setLoadingState(isScreenLoading = true)
            onboardingInteractor.currentFlow = OnboardingFlow.RestoreWallet.SocialPlusCustomShare
            restoreWalletInteractor.obtainTorusKey(userId = userId, idToken = idToken)
            restoreUserWithShares(onboardingInteractor.currentFlow as OnboardingFlow.RestoreWallet)
            view?.setLoadingState(isScreenLoading = false)
        }
    }

    private suspend fun restoreUserWithShares(currentFlow: OnboardingFlow.RestoreWallet) {
        onboardingInteractor.currentFlow = currentFlow
        val restoreResult = restoreWalletInteractor.tryRestoreUser(currentFlow)
        when (val restoreHandledState = restoreUserResultHandler.handleRestoreResult(restoreResult)) {
            is RestoreSuccessState -> {
                updateDeviceShare()
            }
            is RestoreFailureState.TitleSubtitleError -> {
                if (restoreHandledState.email != null) {
                    view?.showIncorrectAccountScreen(restoreHandledState.email)
                } else {
                    view?.showUiKitSnackBar(message = restoreHandledState.title)
                }
            }
            is RestoreFailureState.ToastError -> {
                view?.showUiKitSnackBar(message = restoreHandledState.message)
            }
            is RestoreFailureState.LogError -> {
                Timber.e(RestoreError(restoreHandledState.message))
            }
        }
    }

    private suspend fun updateDeviceShare() {
        val currentMetadata = metadataInteractor.currentMetadata ?: return
        val newMetadata = currentMetadata.copy(
            deviceShareDeviceName = DeviceInfoHelper.getCurrentDeviceName(),
            deviceNameTimestampSec = DateTimeUtils.getCurrentTimestampInSeconds()
        )
        refreshDeviceShare(newMetadata)
        view?.showSuccessDeviceChange()
    }

    private suspend fun refreshDeviceShare(newMetadata: GatewayOnboardingMetadata) {
        try {
            restoreWalletInteractor.refreshDeviceShare(newMetadata)
            metadataInteractor.updateMetadata(newMetadata)
        } catch (error: Throwable) {
            view?.showFailDeviceChange()
            Timber.e(error, "Error on refreshDeviceShare")
        }
    }
}
