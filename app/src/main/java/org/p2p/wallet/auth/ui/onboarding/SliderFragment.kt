package org.p2p.wallet.auth.ui.onboarding

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentOnboardingSlideBinding
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber

class SliderFragment : BaseFragment(R.layout.fragment_onboarding_slide) {

    companion object {
        fun create(): SliderFragment = SliderFragment()
    }

    private val binding: FragmentOnboardingSlideBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { args ->
            val sliderFragmentArgs = SliderFragmentArgs.fromBundle(args)
            with(binding) {
                imageViewSliderIcon.setImageResource(sliderFragmentArgs.iconRes)
                textViewSliderTitle.setText(sliderFragmentArgs.slideTitle)
                textViewSliderSubtitle.setText(sliderFragmentArgs.slideText)
            }
        } ?: Timber.w("Error on getting SliderFragmentArgs")
    }
}
