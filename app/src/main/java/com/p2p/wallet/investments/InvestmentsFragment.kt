package com.p2p.wallet.investments

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentInvestmentsBinding
import com.p2p.wallet.utils.viewbinding.viewBinding

class InvestmentsFragment : BaseFragment(R.layout.fragment_investments) {

    private val binding: FragmentInvestmentsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
        }
    }
}