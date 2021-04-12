package com.p2p.wallet.auth.ui

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.backupwallat.recoverywallat.view.RecoveryWalletFragment
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentOnboardingBinding
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding

class OnboardingFragment : BaseFragment(R.layout.fragment_onboarding) {

    private val binding: FragmentOnboardingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            createButton.setOnClickListener {
                replaceFragment(TermsFragment())
            }
            loginButton.setOnClickListener {
                replaceFragment(RecoveryWalletFragment())
            }
        }
    }
}