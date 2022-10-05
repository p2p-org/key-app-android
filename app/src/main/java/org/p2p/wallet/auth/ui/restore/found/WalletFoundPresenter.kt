package org.p2p.wallet.auth.ui.restore.found

import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.restore.TorusKeyInteractor
import org.p2p.wallet.auth.model.OnboardingFlow

class WalletFoundPresenter(
    private val userSignUpInteractor: UserSignUpInteractor,
    private val signUpFlowDataRepository: SignUpFlowDataLocalRepository,
    private val onboardingInteractor: OnboardingInteractor,
    private val torusKeyInteractor: TorusKeyInteractor
) : BasePresenter<WalletFoundContract.View>(), WalletFoundContract.Presenter {

    override fun attach(view: WalletFoundContract.View) {
        super.attach(view)
        Timber.tag("______wallet found").d(signUpFlowDataRepository.signUpUserId.orEmpty())
        view.setUserId(signUpFlowDataRepository.signUpUserId.orEmpty())
    }

    override fun useAnotherGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun setAlternativeIdToken(userId: String, idToken: String) {
        launch {
            view?.setLoadingState(isScreenLoading = true)
            torusKeyInteractor.getTorusKey(idToken, userId)
            when (val result = userSignUpInteractor.trySignUpNewUser(userId)) {
                UserSignUpInteractor.SignUpResult.SignUpSuccessful -> {
                    onboardingInteractor.currentFlow = OnboardingFlow.CreateWallet
                    view?.onSuccessfulSignUp()
                }
                is UserSignUpInteractor.SignUpResult.SignUpFailed -> {
                    Timber.e(result, "Setting alternative user failed")
                    view?.showUiKitSnackBar(
                        messageResId = R.string.error_general_message
                    )
                }
                UserSignUpInteractor.SignUpResult.UserAlreadyExists -> {
                    view?.onSameTokenFoundError()
                }
            }
            view?.setLoadingState(isScreenLoading = false)
        }
    }

    override fun startRestoreWallet() {
        onboardingInteractor.currentFlow = OnboardingFlow.RestoreWallet()
    }
}
