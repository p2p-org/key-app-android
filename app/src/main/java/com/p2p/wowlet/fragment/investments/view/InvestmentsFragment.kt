package com.p2p.wowlet.fragment.investments.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentInvestmentsBinding
import com.p2p.wowlet.fragment.investments.viewmodel.InvestmentsViewModel
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class InvestmentsFragment : BaseFragment(R.layout.fragment_investments) {

    private val viewModel: InvestmentsViewModel by viewModel()
    private val binding: FragmentInvestmentsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {

        }
    }
}