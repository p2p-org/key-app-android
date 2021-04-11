package com.p2p.wowlet.auth.ui

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.backupwallat.recoverywallat.view.RecoveryWalletFragment
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentOnboardingBinding
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding

class OnboardingFragment : BaseFragment(R.layout.fragment_onboarding) {

    private val binding: FragmentOnboardingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            createButton.setOnClickListener {
                replaceFragment(EnterUsernameFragment.create())
            }
            loginButton.setOnClickListener {
                replaceFragment(RecoveryWalletFragment())
            }
        }
    }
}