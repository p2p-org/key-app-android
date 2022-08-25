package org.p2p.wallet.auth.ui.onboarding

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewOnboardingContract {
    interface View : MvpView {
        fun startGoogleFlow()
        fun onSameTokenFoundError()
        fun onSuccessfulSignUp()
        fun setButtonLoadingState(isScreenLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun onSignUpButtonClicked()
        fun onSignInButtonClicked()
        fun setIdToken(userId: String, idToken: String)
    }
}
