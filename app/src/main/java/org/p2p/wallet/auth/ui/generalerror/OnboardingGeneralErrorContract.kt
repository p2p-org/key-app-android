package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.io.File

interface OnboardingGeneralErrorContract {
    interface View : MvpView {
        fun updateText(title: String, message: String)
        fun setViewState(errorState: GeneralErrorScreenError)
        fun showFile(file: File)
    }

    interface Presenter : MvpPresenter<View> {
        fun onTermsClick()
        fun onPolicyClick()
    }
}
