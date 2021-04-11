package com.p2p.wowlet.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.auth.ui.createwallet.view.CreateWalletFragment
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentTermAndConditionBinding
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding

class TermsAndConditionFragment : BaseFragment(R.layout.fragment_term_and_condition) {
    private val binding: FragmentTermAndConditionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            vIcBack.setOnClickListener { popBackStack() }
            acceptButton.setOnClickListener { replaceFragment(CreateWalletFragment()) }
            declineButton.setOnClickListener { popBackStack() }
        }
    }
}