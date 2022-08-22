package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingGeneralErrorContract {
    interface View : MvpView {
        fun updateTitle(title: String)
        fun updateSubtitle(subTitle: String)
    }

    interface Presenter : MvpPresenter<View>
}
