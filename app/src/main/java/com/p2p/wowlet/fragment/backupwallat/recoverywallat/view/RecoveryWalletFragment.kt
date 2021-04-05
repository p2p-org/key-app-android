package com.p2p.wowlet.fragment.backupwallat.recoverywallat.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentRecoveryWalletBinding
import com.p2p.wowlet.fragment.backupwallat.secretkeys.view.SecretKeyFragment
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding

class RecoveryWalletFragment : BaseFragment(R.layout.fragment_recovery_wallet) {

    private val binding: FragmentRecoveryWalletBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backImageView.setOnClickListener { popBackStack() }
        binding.btBackupManual.setOnClickListener { replaceFragment(SecretKeyFragment()) }
    }
}