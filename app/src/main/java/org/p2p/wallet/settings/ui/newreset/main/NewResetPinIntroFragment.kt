package org.p2p.wallet.settings.ui.newreset.main

import android.os.Bundle
import android.view.View
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentNewResetPinIntroBinding
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.settings.ui.newreset.pin.NewResetPinFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class NewResetPinIntroFragment : BaseFragment(R.layout.fragment_new_reset_pin_intro) {

    companion object {
        fun create(): NewResetPinIntroFragment = NewResetPinIntroFragment()
    }

    override val customNavigationBarStyle: SystemIconsStyle = SystemIconsStyle.WHITE
    private val binding: FragmentNewResetPinIntroBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            sliderChangePin.onSlideCompleteListener = {
                replaceFragment(NewResetPinFragment.create())
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
