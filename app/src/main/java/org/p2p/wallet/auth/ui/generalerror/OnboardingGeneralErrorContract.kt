package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingGeneralErrorContract {
    interface View : MvpView {
        fun setState(state: GatewayHandledState.TitleSubtitleError)
        fun setState(state: GatewayHandledState.CriticalError)
        fun navigateToEnterPhone()
        fun navigateToStartScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun onEnterPhoneClicked()
        fun onStartScreenClicked()
    }
}
