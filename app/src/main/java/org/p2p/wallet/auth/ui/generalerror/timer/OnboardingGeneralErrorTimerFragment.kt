package org.p2p.wallet.auth.ui.generalerror.timer

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.addCallback
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerContract.Presenter
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingGeneralErrorTimerBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.openFile
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.io.File
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerContract.View as ContractView

private const val ARG_TIMER_ERROR_TYPE = "ARG_TIMER_ERROR_TYPE"
private const val ARG_TIMER_LEFT_TIME = "ARG_TIMER_LEFT_TIME"

class OnboardingGeneralErrorTimerFragment :
    BaseMvpFragment<ContractView, Presenter>(R.layout.fragment_onboarding_general_error_timer),
    ContractView {

    companion object {
        fun create(error: GeneralErrorTimerScreenError, timerLeftTime: Long): OnboardingGeneralErrorTimerFragment =
            OnboardingGeneralErrorTimerFragment().withArgs(
                ARG_TIMER_ERROR_TYPE to error,
                ARG_TIMER_LEFT_TIME to timerLeftTime
            )
    }

    override val presenter: Presenter by inject { parametersOf(error, timerLeftTime) }

    private val binding: FragmentOnboardingGeneralErrorTimerBinding by viewBinding()
    private val error: GeneralErrorTimerScreenError by args(ARG_TIMER_ERROR_TYPE)
    private val timerLeftTime: Long by args(ARG_TIMER_LEFT_TIME)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.menu_onboarding_help)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.helpItem) {
                IntercomService.showMessenger()
            }
            return@setOnMenuItemClickListener true
        }
        binding.buttonToStartingScreen.setOnClickListener {
            navigateToStartingScreen()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToStartingScreen()
        }
    }

    private fun FragmentOnboardingGeneralErrorTimerBinding.initViews() {
        textViewTermsAndPolicy.apply {
            text = SpanUtils.buildTermsAndPolicyText(
                context = requireContext(),
                onTermsClick = { presenter.onTermsClick() },
                onPolicyClick = { presenter.onPolicyClick() }
            )
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun updateText(titleRes: Int, subTitleRes: Int, formattedTimeLeft: String) {
        binding.textViewErrorTitle.text = getString(titleRes)
        binding.textViewErrorSubtitle.text = getString(subTitleRes, formattedTimeLeft)
    }

    override fun navigateToStartingScreen() {
        popAndReplaceFragment(OnboardingRootFragment.create(), inclusive = true)
    }

    override fun showFile(file: File) {
        openFile(file)
    }
}
