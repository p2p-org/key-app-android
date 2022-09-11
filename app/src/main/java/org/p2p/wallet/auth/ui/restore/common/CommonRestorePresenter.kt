package org.p2p.wallet.auth.ui.restore.common

import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.SocialShareRestoreInteractor
import org.p2p.wallet.auth.interactor.restore.UserRestoreInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.common.mvp.BasePresenter

class CommonRestorePresenter(
    private val onboardingInteractor: OnboardingInteractor,
    private val socialShareRestoreInteractor: SocialShareRestoreInteractor,
    private val restoreUserRestoreInteractor: UserRestoreInteractor,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository
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
        restoreFlowDataLocalRepository.generateRestoreUserKeyPair()
    }

    override fun setGoogleIdToken(userId: String, idToken: String) {
        view?.setLoadingState(isScreenLoading = true)
        socialShareRestoreInteractor.restoreSocialShare(idToken, userId)
        view?.setLoadingState(isScreenLoading = false)
        if (restoreUserRestoreInteractor.isUserReadyToBeRestored()) {
            // navigate to pin if social + device share exists
        } else {
            view?.navigateToPhoneEnter()
        }
    }
}
