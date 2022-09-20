package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingGeneralErrorContract {
    interface View : MvpView {
        fun updateText(title: String, message: String)
        fun setViewState(errorState: GeneralErrorScreenError)
    }

    interface Presenter : MvpPresenter<View>
}
