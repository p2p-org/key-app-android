package org.p2p.wallet.auth.ui.generalerror

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

class OnboardingGeneralErrorPresenter(
    private val error: GeneralErrorScreenError,
    private val resourcesProvider: ResourcesProvider,
    private val restoreWalletInteractor: RestoreWalletInteractor
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

    override fun setGoogleIdToken(userId: String, idToken: String) {
        view?.setRestoreByGoogleLoadingState(isRestoringByGoogle = true)
        restoreWalletInteractor.restoreSocialShare(userId = userId, idToken = idToken)
        if (restoreWalletInteractor.isUserReadyToBeRestored(OnboardingFlow.RestoreWallet.DevicePlusSocialShare)) {
            launch {
                restoreUserWithShares()
            }
        } else {
            view?.onNoTokenFoundError(userId)
            view?.setRestoreByGoogleLoadingState(isRestoringByGoogle = false)
        }
    }

    private suspend fun restoreUserWithShares() {
        when (val result = restoreWalletInteractor.tryRestoreUser(OnboardingFlow.RestoreWallet.DevicePlusSocialShare)) {
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
            }
        }
    }
}
