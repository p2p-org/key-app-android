package org.p2p.wallet.auth.ui.restore.common

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.SocialShareRestoreInteractor
import org.p2p.wallet.auth.interactor.restore.UserRestoreInteractor
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

class CommonRestorePresenter(
    private val onboardingInteractor: OnboardingInteractor,
    private val socialShareRestoreInteractor: SocialShareRestoreInteractor,
    private val restoreUserRestoreInteractor: UserRestoreInteractor,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository
) : BasePresenter<CommonRestoreContract.View>(), CommonRestoreContract.Presenter {

    override fun useGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun switchFlowToRestore() {
        onboardingInteractor.currentFlow = OnboardingInteractor.OnboardingFlow.RESTORE_WALLET
        restoreFlowDataLocalRepository.generateRestoreUserKeyPair()
    }

    override fun setGoogleIdToken(userId: String, idToken: String) {
        view?.setLoadingState(isScreenLoading = true)
        socialShareRestoreInteractor.restoreSocialShare(idToken, userId)
        if (restoreUserRestoreInteractor.isUserReadyToBeRestored()) {
            launch {
                restoreUserWithShares()
            }
        } else {
            view?.navigateToPhoneEnter()
            view?.setLoadingState(isScreenLoading = false)
        }
    }

    private suspend fun restoreUserWithShares() {
        val result = restoreUserRestoreInteractor.tryRestoreUser(
            UserRestoreInteractor.RestoreUserWay.DevicePlusSocialShareWay
        )
        when (result) {
            is UserRestoreInteractor.RestoreUserResult.RestoreSuccessful -> {
                restoreUserRestoreInteractor.finishAuthFlow()
                view?.setLoadingState(isScreenLoading = false)
                view?.navigateToPinCreate()
            }
            is UserRestoreInteractor.RestoreUserResult.RestoreFailed -> {
                Timber.e(result)
                view?.setLoadingState(isScreenLoading = false)
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }
}
