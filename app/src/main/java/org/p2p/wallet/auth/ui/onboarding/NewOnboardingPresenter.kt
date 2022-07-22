package org.p2p.wallet.auth.ui.onboarding

import org.p2p.wallet.auth.common.WalletWeb3AuthManager
import org.p2p.wallet.auth.model.GoogleAuthFlow
import org.p2p.wallet.common.mvp.BasePresenter

class NewOnboardingPresenter(
    private val walletAuthManager: WalletWeb3AuthManager,
) : BasePresenter<NewOnboardingContract.View>(), NewOnboardingContract.Presenter {

    override fun attach(view: NewOnboardingContract.View) {
        super.attach(view)
        walletAuthManager.attach()
    }

    override fun onSignUpButtonClicked() {
        walletAuthManager.flowMode = GoogleAuthFlow.SIGN_UP
        view?.startGoogleFlow()
    }

    override fun onSignInButtonClicked() {
        walletAuthManager.flowMode = GoogleAuthFlow.SIGN_IN
        view?.startGoogleFlow()
    }

    override fun setIdToken(userId: String, idToken: String) {
        walletAuthManager.setIdToken(userId, idToken)
    }

    override fun detach() {
        walletAuthManager.detach()
        super.detach()
    }
}
