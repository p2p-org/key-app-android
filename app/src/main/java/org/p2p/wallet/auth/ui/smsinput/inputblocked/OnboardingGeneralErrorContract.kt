package org.p2p.wallet.auth.ui.smsinput.inputblocked

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingGeneralErrorContract {
    enum class SourceScreen {
        PHONE_NUMBER_ENTER, SMS_INPUT
    }

    interface View : MvpView {
        fun updateSubtitle(@StringRes subTitleRes: Int, formattedTimeLeft: String)
        fun navigateToStartingScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun setSourceScreen(sourceScreen: SourceScreen)
    }
}
