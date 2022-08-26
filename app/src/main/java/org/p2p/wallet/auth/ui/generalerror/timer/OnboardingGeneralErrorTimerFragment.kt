package org.p2p.wallet.auth.ui.generalerror.timer

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerContract.Presenter
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingGeneralErrorTimerBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerContract.View as ContractView

private const val ARG_TIMER_ERROR_TYPE = "ARG_TIMER_ERROR_TYPE"
private const val ARG_PHONE_NUMBER = "ARG_PHONE_NUMBER"
private const val ARG_COUNTRY_CODE = "ARG_COUNTRY_CODE"

class OnboardingGeneralErrorTimerFragment :
    BaseMvpFragment<ContractView, Presenter>(R.layout.fragment_onboarding_general_error_timer),
    ContractView {

    companion object {
        fun create(
            sourceScreen: GeneralErrorTimerScreenError,
            countryCode: CountryCode?,
            phoneNumber: String?
        ): OnboardingGeneralErrorTimerFragment = OnboardingGeneralErrorTimerFragment()
            .withArgs(
                ARG_TIMER_ERROR_TYPE to sourceScreen,
                ARG_COUNTRY_CODE to countryCode,
                ARG_PHONE_NUMBER to phoneNumber
            )
    }

    override val statusBarColor: Int = R.color.bg_lime
    override val navBarColor: Int = R.color.bg_night
    override val presenter: Presenter by inject { parametersOf(this, error) }

    private val binding: FragmentOnboardingGeneralErrorTimerBinding by viewBinding()
    private val error: GeneralErrorTimerScreenError by args(ARG_TIMER_ERROR_TYPE)

    private val phoneNumber by args<String?>(ARG_PHONE_NUMBER, null)
    private val countryCode by args<CountryCode?>(ARG_COUNTRY_CODE, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonToStartingScreen.setOnClickListener {
            navigateToPhoneNumberEnter()
        }
    }

    override fun updateSubtitle(@StringRes subTitleRes: Int, formattedTimeLeft: String) {
        binding.textViewErrorSubtitle.text = getString(subTitleRes, formattedTimeLeft)
    }

    override fun navigateToPhoneNumberEnter() {
        popAndReplaceFragment(
            PhoneNumberEnterFragment.create(countryCode, phoneNumber), inclusive = true
        )
    }
}
