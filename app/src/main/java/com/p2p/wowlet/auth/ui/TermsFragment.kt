package com.p2p.wowlet.auth.ui

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentTermsBinding
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.viewbinding.viewBinding

class TermsFragment : BaseFragment(R.layout.fragment_terms) {

    private val binding: FragmentTermsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            backImageView.setOnClickListener { popBackStack() }
        }
    }
}