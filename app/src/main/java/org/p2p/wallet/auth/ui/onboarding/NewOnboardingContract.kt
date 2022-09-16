package org.p2p.wallet.auth.ui.onboarding

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.io.File

interface NewOnboardingContract {
    interface View : MvpView {
        fun startGoogleFlow()
        fun onSameTokenFoundError()
        fun onSuccessfulSignUp()
        fun setButtonLoadingState(isScreenLoading: Boolean)
        fun showFile(file: File)
    }

    interface Presenter : MvpPresenter<View> {
        fun onSignUpButtonClicked()
        fun onSignInButtonClicked()
        fun setIdToken(userId: String, idToken: String)
        fun onTermsClick()
        fun onPolicyClick()
    }
}
