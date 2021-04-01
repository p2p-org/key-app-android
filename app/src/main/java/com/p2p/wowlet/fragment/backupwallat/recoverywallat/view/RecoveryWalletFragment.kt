package com.p2p.wowlet.fragment.backupwallat.recoverywallat.view

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentRecoveryWalletBinding
import com.p2p.wowlet.fragment.backupwallat.recoverywallat.viewmodel.RecoveryWalletViewModel
import com.p2p.wowlet.fragment.backupwallat.secretkeys.view.SecretKeyFragment
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replace
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.ArrayList


class RecoveryWalletFragment :
    FragmentBaseMVVM<RecoveryWalletViewModel, FragmentRecoveryWalletBinding>() {


    override val viewModel: RecoveryWalletViewModel by viewModel()
    override val binding: FragmentRecoveryWalletBinding by dataBinding(R.layout.fragment_recovery_wallet)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@RecoveryWalletFragment.viewModel
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> popBackStack()
            is Command.NavigateSecretKeyViewCommand -> {
                replace(SecretKeyFragment())
            }
        }
    }

}