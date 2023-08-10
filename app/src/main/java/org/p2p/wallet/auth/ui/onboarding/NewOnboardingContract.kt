package org.p2p.wallet.auth.ui.onboarding

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewOnboardingContract {
    interface View : MvpView {
        fun startGoogleFlow()
        fun onSameTokenFoundError()
        fun onSuccessfulSignUp()
        fun setButtonLoadingState(isScreenLoading: Boolean)
        fun navigateToContinueCreateWallet()
        fun showBrowserTab(url: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun onSignUpButtonClicked()
        fun onSignInButtonClicked()
        fun setIdToken(userId: String, googleIdJwtToken: String)
        fun onTermsClick()
        fun onPolicyClick()
    }
}
