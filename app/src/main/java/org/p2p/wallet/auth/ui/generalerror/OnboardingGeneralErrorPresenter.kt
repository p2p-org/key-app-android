package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.repository.RestoreUserExceptionHandler
import org.p2p.wallet.common.mvp.BasePresenter

class OnboardingGeneralErrorPresenter(
    private val error: GeneralErrorScreenError,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreUserExceptionHandler: RestoreUserExceptionHandler
) : BasePresenter<OnboardingGeneralErrorContract.View>(),
    OnboardingGeneralErrorContract.Presenter {

//    override fun useGoogleAccount() {
//        view?.startGoogleFlow()
//    }
//
//    override fun onDevicePlusCustomShareRestoreClicked() {
//        onboardingInteractor.currentFlow = OnboardingFlow.RestoreWallet.DevicePlusCustomShare
//        view?.navigateToEnterPhone()
//    }
//
//    override fun onContinueWithPhoneNumberClicked() {
//        restoreWalletInteractor.resetUserPhoneNumber()
//        view?.navigateToEnterPhone()
//    }
//
//    override fun setGoogleIdToken(userId: String, idToken: String) {
//        launch {
//            view?.setRestoreByGoogleLoadingState(isRestoringByGoogle = true)
//            restoreWalletInteractor.obtainTorusKey(userId = userId, idToken = idToken)
//
//            val flow = if (restoreWalletInteractor.isDeviceShareSaved()) {
//                OnboardingFlow.RestoreWallet.DevicePlusSocialShare
//            } else {
//                OnboardingFlow.RestoreWallet.SocialPlusCustomShare
//            }
//            if (restoreWalletInteractor.isUserReadyToBeRestored(flow)) {
//                restoreUserWithShares(flow)
//            } else {
//                // view?.onNoTokenFoundError(userId)
//                view?.setRestoreByGoogleLoadingState(isRestoringByGoogle = false)
//            }
//        }
//    }
//
//    private suspend fun restoreUserWithShares(flow: OnboardingFlow.RestoreWallet) {
//        val result = restoreWalletInteractor.tryRestoreUser(flow)
//        when (val state = restoreUserExceptionHandler.handleRestoreResult(result)) {
//            is RestoreFailureState.TitleSubtitleError -> {
//                view?.showState(state)
//            }
//            is RestoreSuccessState -> {
//                view?.navigateToPinCreate()
//            }
//        }
//    }
}
