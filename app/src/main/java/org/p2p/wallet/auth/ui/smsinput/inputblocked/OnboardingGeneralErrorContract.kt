package org.p2p.wallet.auth.ui.smsinput.inputblocked

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingGeneralErrorContract {
    interface View : MvpView {
        enum class SourceScreen {
            PHONE_NUMBER_ENTER, SMS_INPUT
        }

        fun renderTimerBeforeUnblock(formattedTime: String)
        fun setTitleAndSubtitle(@StringRes titleRes: Int, @StringRes subTitleRes: Int)
        fun navigateToStartingScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun setSourceScreen(sourceScreen: View.SourceScreen)
    }
}
