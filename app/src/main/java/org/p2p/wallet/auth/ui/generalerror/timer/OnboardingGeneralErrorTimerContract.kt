package org.p2p.wallet.auth.ui.generalerror.timer

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingGeneralErrorTimerContract {
    interface View : MvpView {
        fun updateSubtitle(@StringRes subTitleRes: Int, formattedTimeLeft: String)
        fun navigateToStartingScreen()
    }

    interface Presenter : MvpPresenter<View>
}
