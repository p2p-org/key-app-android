package org.p2p.wallet.auth.ui.restore

import org.p2p.wallet.auth.common.WalletWeb3AuthManager
import org.p2p.wallet.auth.common.Web3AuthError
import org.p2p.wallet.auth.common.Web3AuthHandler
import org.p2p.wallet.auth.model.GoogleAuthFlow
import org.p2p.wallet.common.mvp.BasePresenter

class WalletFoundPresenter(
    private val walletAuthManager: WalletWeb3AuthManager,
) : BasePresenter<WalletFoundContract.View>(), WalletFoundContract.Presenter {

    private val web3AuthHandler = object : Web3AuthHandler {
        override fun onSuccessSignUp() {
            view?.onSuccessfulSignUp()
        }

        override fun handleError(error: Web3AuthError) {
            view?.showError(error.errorMessage)
        }
    }

    override fun attach(view: WalletFoundContract.View) {
        super.attach(view)
        walletAuthManager.attach()
        walletAuthManager.addHandler(web3AuthHandler)
        view.setUserId(walletAuthManager.userId.orEmpty())
    }

    override fun onSignUpButtonClicked() {
        walletAuthManager.flowMode = GoogleAuthFlow.SIGN_UP
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
