package com.p2p.wallet.auth.ui

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.security.ui.SecurityKeyFragment
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentTermsBinding
import com.p2p.wallet.utils.popAndReplaceFragment
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding

class TermsFragment : BaseFragment(R.layout.fragment_terms) {

    private val binding: FragmentTermsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            acceptButton.setOnClickListener {
                popAndReplaceFragment(SecurityKeyFragment.create())
            }
        }
    }
}