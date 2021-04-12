package com.p2p.wallet.backupwallat.recoverywallat.view

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.backupwallat.secretkeys.view.SecretKeyFragment
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentRecoveryWalletBinding
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding

class RecoveryWalletFragment : BaseFragment(R.layout.fragment_recovery_wallet) {

    private val binding: FragmentRecoveryWalletBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backImageView.setOnClickListener { popBackStack() }
        binding.btBackupManual.setOnClickListener { replaceFragment(SecretKeyFragment()) }
    }
}