package org.p2p.wallet.settings.ui.resetpin.main

import android.os.Bundle
import android.view.View
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentResetPinIntroBinding
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.settings.ui.resetpin.pin.ResetPinFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class ResetPinIntroFragment : BaseFragment(R.layout.fragment_reset_pin_intro) {

    companion object {
        fun create(): ResetPinIntroFragment = ResetPinIntroFragment()
    }

    override val customNavigationBarStyle: SystemIconsStyle = SystemIconsStyle.WHITE
    private val binding: FragmentResetPinIntroBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            sliderChangePin.onSlideCompleteListener = {
                replaceFragment(ResetPinFragment.create())
            }

            sliderChangePin.setLightStyle(isLight = true)
        }
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            insets.systemAndIme().consume {
                binding.toolbar.appleTopInsets(this)
                binding.containerBottomView.appleBottomInsets(this)
            }
        }
    }
}
