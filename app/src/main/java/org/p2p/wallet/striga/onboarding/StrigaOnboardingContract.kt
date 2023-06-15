package org.p2p.wallet.striga.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface StrigaOnboardingContract {

    interface View : MvpView {
        enum class AvailabilityState(
            @DrawableRes val imageRes: Int,
            @StringRes val titleTextRes: Int,
            @StringRes val buttonTextRes: Int,
            val isHelpVisible: Boolean,
            val isPoweredByStrigaVisible: Boolean,
            val isButtonArrowVisible: Boolean
        ) {
            Available(
                imageRes = R.drawable.ic_hand_phone,
                titleTextRes = R.string.striga_onboarding_title_available,
                buttonTextRes = R.string.common_continue,
                isHelpVisible = false,
                isPoweredByStrigaVisible = true,
                isButtonArrowVisible = true,
            ),
            Unavailable(
                imageRes = R.drawable.ic_euro_flag,
                titleTextRes = R.string.striga_onboarding_title_unavailable,
                buttonTextRes = R.string.striga_onboarding_button_change_country,
                isPoweredByStrigaVisible = false,
                isHelpVisible = true,
                isButtonArrowVisible = false
            );
        }

        fun setCurrentCountry(country: CountryCode)
        fun setAvailabilityState(state: AvailabilityState)
        fun showCountryPicker(selectedItem: CountryCode?)
        fun openHelp()
        fun navigateNext()
    }

    interface Presenter : MvpPresenter<View> {
        fun onClickContinue()
        fun onClickHelp()
        fun onCurrentCountryChanged(selectedCountry: CountryCode)
        fun onCountryClicked()
    }
}
