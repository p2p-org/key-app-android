package org.p2p.wallet.auth.ui.generalerror

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

class OnboardingGeneralErrorPresenter(
    private val error: GeneralErrorScreenError,
    private val resourcesProvider: ResourcesProvider,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor
) : BasePresenter<OnboardingGeneralErrorContract.View>(),
    OnboardingGeneralErrorContract.Presenter {

    override fun attach(view: OnboardingGeneralErrorContract.View) {
        super.attach(view)

        when (error) {
            is GeneralErrorScreenError.CriticalError -> {
                val title = resourcesProvider.getString(
                    R.string.onboarding_general_error_critical_error_title
                )
                val subTitle = resourcesProvider.getString(
                    R.string.onboarding_general_error_critical_error_sub_title,
                    error.errorCode
                )
                view.updateText(title, subTitle)
                view.setViewState(error)
            }
            else -> {
                view.setViewState(error)
            }
        }
    }

    override fun useGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun onDevicePlusCustomShareRestoreClicked() {
        onboardingInteractor.currentFlow = OnboardingFlow.RestoreWallet.DevicePlusCustomShare
        view?.navigateToEnterPhone()
    }

    override fun onContinueWithPhoneNumberClicked() {
        restoreWalletInteractor.resetUserPhoneNumber()
        view?.navigateToEnterPhone()
    }

    override fun setGoogleIdToken(userId: String, idToken: String) {
        view?.setRestoreByGoogleLoadingState(isRestoringByGoogle = true)
        restoreWalletInteractor.restoreSocialShare(userId = userId, idToken = idToken)
        val flow = if (restoreWalletInteractor.isDeviceShareSaved()) {
            OnboardingFlow.RestoreWallet.DevicePlusSocialShare
        } else {
            OnboardingFlow.RestoreWallet.SocialPlusCustomShare
        }
        if (restoreWalletInteractor.isUserReadyToBeRestored(flow)) {
            launch {
                restoreUserWithShares(flow)
            }
        } else {
            view?.onNoTokenFoundError(userId)
            view?.setRestoreByGoogleLoadingState(isRestoringByGoogle = false)
        }
    }

    private suspend fun restoreUserWithShares(flow: OnboardingFlow.RestoreWallet) {
        when (val result = restoreWalletInteractor.tryRestoreUser(flow)) {
            is RestoreUserResult.RestoreSuccessful -> {
                restoreWalletInteractor.finishAuthFlow()
                view?.setRestoreByGoogleLoadingState(isRestoringByGoogle = false)
                view?.navigateToPinCreate()
            }
            is RestoreUserResult.RestoreFailed -> {
                Timber.e(result)
                view?.setRestoreByGoogleLoadingState(isRestoringByGoogle = false)
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            RestoreUserResult.UserNotFound -> {
                view?.onNoTokenFoundError(restoreWalletInteractor.getUserEmailAddress().orEmpty())
                view?.setRestoreByGoogleLoadingState(false)
            }
            is RestoreUserResult.SocialShareNotFound -> {
                val error = GeneralErrorScreenError.SocialShareNotFound(result.socialShareUserId)
                view?.setViewState(error)
                view?.setRestoreByGoogleLoadingState(false)
            }
            is RestoreUserResult.DeviceAndSocialShareNotMatch -> {
                val error = GeneralErrorScreenError.SharesDoNotMatchError
                view?.setViewState(error)
                view?.setRestoreByGoogleLoadingState(false)
            }
        }
    }
}
