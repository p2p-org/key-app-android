package org.p2p.wallet.auth.ui.smsinput.inputblocked

import androidx.annotation.StringRes
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import org.p2p.wallet.auth.ui.smsinput.inputblocked.OnboardingGeneralErrorContract.Presenter
import org.p2p.wallet.auth.ui.smsinput.inputblocked.OnboardingGeneralErrorContract.View.SourceScreen
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingGeneralTimerErrorBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.auth.ui.smsinput.inputblocked.OnboardingGeneralErrorContract.View as ContractView

class OnboardingGeneralErrorTimerFragment :
    BaseMvpFragment<ContractView, Presenter>(R.layout.fragment_onboarding_general_timer_error),
    ContractView {

    companion object {
        private const val ARG_SOURCE_SCREEN = "ARG_SOURCE_SCREEN"

        fun create(sourceScreen: SourceScreen): OnboardingGeneralErrorTimerFragment =
            OnboardingGeneralErrorTimerFragment()
                .withArgs(ARG_SOURCE_SCREEN to sourceScreen)
    }

    private val binding: FragmentOnboardingGeneralTimerErrorBinding by viewBinding()

    override val presenter: Presenter by inject()

    private val sourceScreen: SourceScreen by args(ARG_SOURCE_SCREEN)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonToStartingScreen.setOnClickListener {
            navigateToStartingScreen()
        }

        presenter.setSourceScreen(sourceScreen)
    }

    override fun setTitleAndSubtitle(@StringRes titleRes: Int, @StringRes subTitleRes: Int) {
        binding.textViewErrorTitle.setText(titleRes)
        binding.textViewErrorSubtitle.setText(subTitleRes)
    }

    override fun renderTimerBeforeUnblock(formattedTime: String) {
        binding.textViewErrorSubtitle.text = getString(
            R.string.onboarding_sms_input_blocked_timer_subtitle,
            formattedTime
        )
    }

    override fun navigateToStartingScreen() {
        popAndReplaceFragment(OnboardingFragment.create(), addToBackStack = false)
    }
}
