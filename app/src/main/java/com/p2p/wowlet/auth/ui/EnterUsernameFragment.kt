package com.p2p.wowlet.auth.ui

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentEnterUsernameBinding
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.viewbinding.viewBinding

class EnterUsernameFragment : BaseFragment(R.layout.fragment_enter_username) {

    companion object {
        fun create() = EnterUsernameFragment()
    }

    private val binding: FragmentEnterUsernameBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            backImageView.clipToOutline = true
            backImageView.setOnClickListener { popBackStack() }
        }
    }
}