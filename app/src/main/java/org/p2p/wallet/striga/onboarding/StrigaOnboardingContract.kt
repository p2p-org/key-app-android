package org.p2p.wallet.striga.onboarding

import androidx.annotation.StringRes
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface StrigaOnboardingContract {

    interface View : MvpView {
        enum class ButtonState(
            @StringRes val textRes: Int,
            val showArrowRight: Boolean
        ) {
            Continue(R.string.common_continue, true),
            ChangeCountry(R.string.striga_onboarding_button_change_country, false);
        }

        fun setCurrentCountry(country: Country)
        fun setButtonState(state: ButtonState)
        fun openCountrySelection()
        fun navigateNext()
    }

    interface Presenter : MvpPresenter<View> {
        fun onCountrySelected(country: Country)
        fun onClickContinue()
        fun onClickChangeCountry()
    }
}
