package com.p2p.wowlet.auth.ui

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentRegFinishBinding
import com.p2p.wowlet.home.HomeFragment
import com.p2p.wowlet.utils.popAndReplaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding

class RegFinishFragment : BaseFragment(R.layout.fragment_reg_finish) {

    private val binding: FragmentRegFinishBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btFinish.setOnClickListener {
            popAndReplaceFragment(HomeFragment.create(), inclusive = true)
        }
    }
}