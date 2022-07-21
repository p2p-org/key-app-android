package org.p2p.wallet.auth.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.databinding.FragmentNewOnboardingBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class NewOnboardingFragment : BaseFragment(R.layout.fragment_new_onboarding) {

    companion object {
        fun create(): NewOnboardingFragment = NewOnboardingFragment()
    }

    private val binding: FragmentNewOnboardingBinding by viewBinding()
    private val analytics: OnboardingAnalytics by inject()

    private val fragments = List(3) { SliderFragment::class }
    private val args = List(3) {
        SliderFragmentArgs(
            R.drawable.bg_auth_done,
            R.string.onboarding_slide_1_title,
            R.string.onboarding_slide_1_text,
        ).toBundle()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analytics.logSplashViewed()

        with(binding) {
            onboardingSliderPager.adapter = BaseFragmentAdapter(childFragmentManager, lifecycle, fragments, args)
            onboardingSliderDotsIndicator.attachTo(onboardingSliderPager)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }
}
