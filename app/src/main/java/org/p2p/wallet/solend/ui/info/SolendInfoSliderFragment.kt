package org.p2p.wallet.solend.ui.info

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSolendInfoSlideBinding
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber

class SolendInfoSliderFragment : BaseFragment(R.layout.fragment_solend_info_slide) {

    companion object {
        fun create(): SolendInfoSliderFragment = SolendInfoSliderFragment()
    }

    private val binding: FragmentSolendInfoSlideBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { args ->
            val sliderFragmentArgs = SolendInfoSliderFragmentArgs.fromBundle(args)
            with(binding) {
                imageViewSliderIcon.setImageResource(sliderFragmentArgs.iconRes)
                textViewSliderText.setText(sliderFragmentArgs.slideText)
                textViewSliderTitle.setText(sliderFragmentArgs.slideTitle)
            }
        } ?: Timber.e("Error on getting SolendInfoSliderFragmentArgs")
    }
}
