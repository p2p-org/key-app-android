package org.p2p.wallet.auth.ui.generalerror.timer

import androidx.activity.addCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerContract.Presenter
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.common.NavigationStrategy
import org.p2p.wallet.common.NavigationStrategy.Companion.ARG_NAVIGATION_STRATEGY
import org.p2p.wallet.common.NavigationStrategy.Companion.ARG_NEXT_DESTINATION_CLASS
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingGeneralErrorTimerBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.OnboardingSpanUtils
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerContract.View as ContractView

private const val ARG_TIMER_LEFT_TIME = "ARG_TIMER_LEFT_TIME"

class OnboardingGeneralErrorTimerFragment :
    BaseMvpFragment<ContractView, Presenter>(R.layout.fragment_onboarding_general_error_timer),
    ContractView {

    companion object {
        fun create(
            timerLeftTime: Long,
            destinationFragment: Class<out Fragment>? = null,
            navigationStrategy: NavigationStrategy? = null
        ): OnboardingGeneralErrorTimerFragment {
            val destinationClass = destinationFragment ?: OnboardingRootFragment::class.java
            val defaultStrategy = NavigationStrategy.PopAndReplace(null, true)
            val strategy = navigationStrategy ?: defaultStrategy
            return OnboardingGeneralErrorTimerFragment().withArgs(
                ARG_TIMER_LEFT_TIME to timerLeftTime,
                ARG_NEXT_DESTINATION_CLASS to destinationClass,
                ARG_NAVIGATION_STRATEGY to strategy
            )
        }
    }

    override val presenter: Presenter by inject { parametersOf(timerLeftTime) }

    private val nextDestinationClass: Class<Fragment> by args(ARG_NEXT_DESTINATION_CLASS)
    private val navigationStrategy: NavigationStrategy by args(ARG_NAVIGATION_STRATEGY)

    private val binding: FragmentOnboardingGeneralErrorTimerBinding by viewBinding()
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
        binding.initViews()
    }

    private fun FragmentOnboardingGeneralErrorTimerBinding.initViews() {
        textViewTermsAndPolicy.apply {
            text = OnboardingSpanUtils.buildTermsAndPolicyText(
                context = requireContext(),
                onTermsClick = { presenter.onTermsClick() },
                onPolicyClick = { presenter.onPolicyClick() }
            )
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            val systemAndIme = insets.systemAndIme()
            rootView.updatePadding(top = systemAndIme.top)
            binding.containerBottomButtons.updatePadding(bottom = systemAndIme.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun updateText(titleRes: Int, subTitleRes: Int, formattedTimeLeft: String) {
        binding.textViewErrorTitle.text = getString(titleRes)
        binding.textViewErrorSubtitle.text = getString(subTitleRes, formattedTimeLeft)
    }

    override fun navigateToStartingScreen() {
        navigationStrategy.navigateNext(this, nextDestinationClass)
    }

    override fun showBrowserTab(url: String) {
        showUrlInCustomTabs(url)
    }
}
