package org.p2p.wallet.auth.ui.generalerror

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingGeneralErrorContract {
    interface View : MvpView {
        fun updateTitle(@StringRes titleRes: Int)
        fun updateSubtitle(@StringRes subTitleRes: Int)
        fun navigateToStartingScreen()
    }

    interface Presenter : MvpPresenter<View>
}
