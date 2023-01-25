package org.p2p.wallet.solend.ui.aboutearn.slider

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSolendAboutEarnSliderBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class SolendAboutEarnSliderFragment : BaseFragment(R.layout.fragment_solend_about_earn_slider) {

    companion object {
        fun create(): SolendAboutEarnSliderFragment = SolendAboutEarnSliderFragment()
    }

    private val binding: FragmentSolendAboutEarnSliderBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sliderFragmentArgs = SolendAboutEarnSliderFragmentArgs.fromBundle(requireArguments())
        with(binding) {
            imageViewSliderIcon.setImageResource(sliderFragmentArgs.iconRes)
            textViewSliderTitle.setText(sliderFragmentArgs.slideTitle)
            textViewSliderText.setText(sliderFragmentArgs.slideText)
        }
    }
}
