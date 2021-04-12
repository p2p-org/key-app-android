package com.p2p.wallet.auth.ui

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentRegFinishBinding
import com.p2p.wallet.home.HomeFragment
import com.p2p.wallet.utils.popAndReplaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding

class RegFinishFragment : BaseFragment(R.layout.fragment_reg_finish) {

    private val binding: FragmentRegFinishBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btFinish.setOnClickListener {
            popAndReplaceFragment(HomeFragment.create(), inclusive = true)
        }
    }
}