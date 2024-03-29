package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingGeneralErrorContract {
    interface View : MvpView {
        fun setState(state: GatewayHandledState.TitleSubtitleError)
        fun setState(state: GatewayHandledState.CriticalError)
        fun navigateToEnterPhone()
        fun navigateToStartScreen()
        fun startGoogleFlow()
        fun setLoadingState(isLoading: Boolean)
        fun navigateToPinCreate()
        fun restartWithState(state: RestoreFailureState.TitleSubtitleError)
    }

    interface Presenter : MvpPresenter<View> {
        fun onEnterPhoneClicked()
        fun onStartScreenClicked()
        fun onGoogleAuthClicked()
        fun setGoogleIdToken(userId: String, idToken: String)
    }
}
