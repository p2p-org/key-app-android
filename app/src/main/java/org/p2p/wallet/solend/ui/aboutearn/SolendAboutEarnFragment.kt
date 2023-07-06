package org.p2p.wallet.solend.ui.aboutearn

import androidx.activity.addCallback
import androidx.viewpager2.widget.ViewPager2
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.common.ui.FragmentPageConfiguration
import org.p2p.wallet.databinding.FragmentSolendAboutEarnBinding
import org.p2p.wallet.solend.ui.aboutearn.slider.SolendAboutEarnSliderFragment
import org.p2p.wallet.solend.ui.aboutearn.slider.SolendAboutEarnSliderFragmentArgs
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

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

    private var pageSelected = 0

    private val pages = listOf(
        FragmentPageConfiguration(
            SolendAboutEarnSliderFragment::class,
            SolendAboutEarnSliderFragmentArgs(
                R.drawable.ic_about_earn_1,
                R.string.about_earn_slider_title_1,
                R.string.about_earn_slider_text_1,
            ).toBundle(),
        ),
        FragmentPageConfiguration(
            SolendAboutEarnSliderFragment::class,
            SolendAboutEarnSliderFragmentArgs(
                R.drawable.ic_about_earn_2,
                R.string.about_earn_slider_title_2,
                R.string.about_earn_slider_text_2,
            ).toBundle(),
        ),
        FragmentPageConfiguration(
            SolendAboutEarnSliderFragment::class,
            SolendAboutEarnSliderFragmentArgs(
                R.drawable.ic_about_earn_3,
                R.string.about_earn_slider_title_3,
                R.string.about_earn_slider_text_3,
            ).toBundle()
        ),
    )

    private val viewPagerSliderChangedCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            pageSelected = position
            binding.buttonNext.setText(
                if (position == pages.size - 1) {
                    R.string.about_earn_continue
                } else {
                    R.string.about_earn_next
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            viewPagerSliderFragments.adapter = BaseFragmentAdapter(
                fragmentManager = childFragmentManager,
                lifecycle = lifecycle,
                pages = pages,
            )
            viewPagerSliderFragments.registerOnPageChangeCallback(viewPagerSliderChangedCallback)
            dotsIndicatorSliderFragments.attachTo(viewPagerSliderFragments)

            buttonSkip.setOnClickListener {
                closeOnboarding()
            }

            buttonNext.setOnClickListener {
                if (pageSelected == pages.size - 1) {
                    presenter.onContinueButtonClicked()
                } else {
                    presenter.onNextButtonClicked()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }

    override fun slideNext() {
        with(binding) {
            viewPagerSliderFragments.currentItem = pageSelected + 1
        }
    }

    override fun closeOnboarding() {
        popBackStack()
    }
}
