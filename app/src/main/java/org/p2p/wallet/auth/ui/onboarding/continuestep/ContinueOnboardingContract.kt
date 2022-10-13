package org.p2p.wallet.auth.ui.onboarding.continuestep

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ContinueOnboardingContract {
    interface View : MvpView {
        fun showUserId(userId: String)
        fun navigateToPhoneNumberEnter()
        fun setLoadingState(isScreenLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun continueSignUp()
    }
}
