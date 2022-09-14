package org.p2p.wallet.auth.ui.restore.common

import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter

class CommonRestorePresenter(
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val accountStorageContract: UserSignUpDetailsStorage
) : BasePresenter<CommonRestoreContract.View>(), CommonRestoreContract.Presenter {

    override fun useGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun useCustomShare() {
        onboardingInteractor.currentFlow = OnboardingFlow.RestoreWallet.DevicePlusCustomShare
        view?.navigateToPhoneEnter()
    }

    override fun switchFlowToRestore() {
        onboardingInteractor.currentFlow = OnboardingFlow.RestoreWallet()
        restoreWalletInteractor.generateRestoreUserKeyPair()
    }

    override fun setGoogleIdToken(userId: String, idToken: String) {
        view?.setLoadingState(isScreenLoading = true)
        restoreWalletInteractor.restoreSocialShare(idToken, userId)
        view?.setLoadingState(isScreenLoading = false)
        if (restoreWalletInteractor.isUserReadyToBeRestored(OnboardingFlow.RestoreWallet.SocialPlusCustomShare)) {
            // navigate to pin if social + device share exists
        } else {
            view?.navigateToPhoneEnter()
        }
    }
}
