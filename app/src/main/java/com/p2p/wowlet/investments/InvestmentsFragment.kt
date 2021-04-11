package com.p2p.wowlet.investments

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentInvestmentsBinding
import com.p2p.wowlet.utils.viewbinding.viewBinding

class InvestmentsFragment : BaseFragment(R.layout.fragment_investments) {

    private val binding: FragmentInvestmentsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
        }
    }
}