package org.p2p.wallet.auth.ui.restore.common

import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

class CommonRestorePresenter(
    private val userSignUpInteractor: UserSignUpInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository
) : BasePresenter<CommonRestoreContract.View>(), CommonRestoreContract.Presenter {

    override fun useGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun switchFlowToRestore() {
        onboardingInteractor.currentFlow = OnboardingInteractor.OnboardingFlow.RESTORE_WALLET
        restoreFlowDataLocalRepository.generateRestoreUserKeyPair()
    }

    override fun setAlternativeIdToken(userId: String, idToken: String) {
        launch {
            // TODO modify logic with userSignInInteractor
            when (val result = userSignUpInteractor.trySignUpNewUser(idToken, userId)) {
                is UserSignUpInteractor.SignUpResult.SignUpSuccessful -> {
                    view?.onSuccessfulSignUp()
                }
                is UserSignUpInteractor.SignUpResult.SignUpFailed -> {
                    Timber.e(result, "Restoring account failed")
                    view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                }
                UserSignUpInteractor.SignUpResult.UserAlreadyExists -> {
                    view?.onNoTokenFoundError(userId)
                }
            }
        }
    }
}
