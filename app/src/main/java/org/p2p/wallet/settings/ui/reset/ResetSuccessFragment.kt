package org.p2p.wallet.settings.ui.reset

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentResetPinSuccessBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class ResetSuccessFragment : BaseFragment(R.layout.fragment_reset_pin_success) {

    companion object {
        fun create() = ResetSuccessFragment()
    }

    private val binding: FragmentResetPinSuccessBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            backButton.setOnClickListener { popBackStack() }
        }
    }
}
