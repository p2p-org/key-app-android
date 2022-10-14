package org.p2p.wallet.solend.ui.aboutearn

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.viewpager2.widget.ViewPager2
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.SliderFragmentArgs
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.databinding.FragmentSolendAboutEarnBinding
import org.p2p.wallet.solend.ui.aboutearn.slider.SolendAboutEarnSliderFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber

class SolendAboutEarnFragment :
    BaseMvpFragment<SolendAboutEarnContract.View, SolendAboutEarnContract.Presenter>(
        R.layout.fragment_solend_about_earn
    ),
    SolendAboutEarnContract.View {

    companion object {
        fun create(): SolendAboutEarnFragment = SolendAboutEarnFragment()
    }

    override val presenter: SolendAboutEarnContract.Presenter by inject()

    private val binding: FragmentSolendAboutEarnBinding by viewBinding()

    private val sliderFragments = List(3) { SolendAboutEarnSliderFragment::class }
    private val sliderFragmentArgs = List(3) {
        SliderFragmentArgs(
            R.drawable.bg_auth_done,
            R.string.onboarding_slide_1_title,
            R.string.onboarding_slide_1_text,
        ).toBundle()
    }

    private val viewPagerSliderChangedCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Timber.d("$position")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            viewPagerSliderFragments.adapter = BaseFragmentAdapter(
                fragmentManager = childFragmentManager,
                lifecycle = lifecycle,
                items = sliderFragments,
                args = sliderFragmentArgs
            )
            viewPagerSliderFragments.registerOnPageChangeCallback(viewPagerSliderChangedCallback)
            dotsIndicatorSliderFragments.attachTo(viewPagerSliderFragments)

            buttonNext.setOnClickListener {
                presenter.onNextButtonClicked()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }
}
