package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.auth.repository.RestoreFailureState
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingGeneralErrorContract {
    interface View : MvpView {
        fun startGoogleFlow()
        fun setRestoreByGoogleLoadingState(isRestoringByGoogle: Boolean)
        fun navigateToPinCreate()
        fun navigateToEnterPhone()
        fun showState(state: RestoreFailureState.TitleSubtitleError)
    }

    interface Presenter : MvpPresenter<View> {
        fun setGoogleIdToken(userId: String, idToken: String)
        fun useGoogleAccount()
        fun onDevicePlusCustomShareRestoreClicked()
        fun onContinueWithPhoneNumberClicked()
    }
}
