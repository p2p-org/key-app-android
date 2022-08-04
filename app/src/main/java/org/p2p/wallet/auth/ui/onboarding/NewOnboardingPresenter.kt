package org.p2p.wallet.auth.ui.onboarding

import org.p2p.wallet.auth.common.WalletWeb3AuthManager
import org.p2p.wallet.auth.common.Web3AuthError
import org.p2p.wallet.auth.common.Web3AuthHandler
import org.p2p.wallet.auth.model.GoogleAuthFlow
import org.p2p.wallet.common.mvp.BasePresenter

private const val WALLET_FOUND = 1009

class NewOnboardingPresenter(
    private val walletAuthManager: WalletWeb3AuthManager,
) : BasePresenter<NewOnboardingContract.View>(), NewOnboardingContract.Presenter {

    private val web3AuthHandler = object : Web3AuthHandler {
        override fun onSuccessSignUp() {
            view?.onSuccessfulSignUp()
        }

        override fun handleError(error: Web3AuthError) {
            if (error.errorCode == WALLET_FOUND) {
                // TODO PWN-4348 make real duplicated token error after this task will be implemented!
                view?.onSameTokenError()
            } else {
                view?.showError(error.errorMessage)
            }
        }
    }

    override fun attach(view: NewOnboardingContract.View) {
        super.attach(view)
        walletAuthManager.attach()
        walletAuthManager.addHandler(web3AuthHandler)
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
        walletAuthManager.removeHandler(web3AuthHandler)
        super.detach()
    }
}
