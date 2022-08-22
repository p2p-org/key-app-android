package org.p2p.wallet.auth.ui.generalerror.timer

import androidx.annotation.StringRes
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerContract.Presenter
import org.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingGeneralErrorTimerBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerContract.View as ContractView

private const val ARG_TIMER_ERROR_TYPE = "ARG_TIMER_ERROR_TYPE"

class OnboardingGeneralErrorTimerFragment :
    BaseMvpFragment<ContractView, Presenter>(R.layout.fragment_onboarding_general_error_timer),
    ContractView {

    companion object {
        fun create(sourceScreen: GeneralErrorTimerScreenError): OnboardingGeneralErrorTimerFragment =
            OnboardingGeneralErrorTimerFragment()
                .withArgs(ARG_TIMER_ERROR_TYPE to sourceScreen)
    }

    private val binding: FragmentOnboardingGeneralErrorTimerBinding by viewBinding()

    private val error: GeneralErrorTimerScreenError by args(ARG_TIMER_ERROR_TYPE)

    override val presenter: Presenter by inject { parametersOf(this, error) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonToStartingScreen.setOnClickListener {
            navigateToStartingScreen()
        }
    }

    override fun updateSubtitle(@StringRes subTitleRes: Int, formattedTimeLeft: String) {
        binding.textViewErrorSubtitle.text = getString(subTitleRes, formattedTimeLeft)
    }

    override fun navigateToStartingScreen() {
        popAndReplaceFragment(OnboardingFragment.create(), inclusive = true)
    }
}
