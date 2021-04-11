package com.p2p.wowlet.fragment.reglogin.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentRegLoginBinding
import com.p2p.wowlet.fragment.backupwallat.recoverywallat.view.RecoveryWalletFragment
import com.p2p.wowlet.fragment.termandcondition.view.TermsAndConditionFragment
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding

class OnboardingFragment : BaseFragment(R.layout.fragment_reg_login) {

    private val binding: FragmentRegLoginBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        with(binding) {
            btCreate.setOnClickListener {
                replaceFragment(TermsAndConditionFragment())
            }
            btAlready.setOnClickListener {
                replaceFragment(RecoveryWalletFragment())
            }
        }
    }
}