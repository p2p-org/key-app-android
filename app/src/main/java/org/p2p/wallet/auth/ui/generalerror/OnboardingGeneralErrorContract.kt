package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingGeneralErrorContract {
    interface View : MvpView {
        fun updateText(title: String, message: String)
        fun setViewState(errorState: GeneralErrorScreenError)
        fun startGoogleFlow()
        fun setRestoreByGoogleLoadingState(isRestoringByGoogle: Boolean)
        fun onNoTokenFoundError(userId: String)
        fun navigateToPinCreate()
        fun navigateToEnterPhone()
    }

    interface Presenter : MvpPresenter<View> {
        fun setGoogleIdToken(userId: String, idToken: String)
        fun useGoogleAccount()
        fun onDevicePlusCustomShareRestoreClicked()
    }
}
