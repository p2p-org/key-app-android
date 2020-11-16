package com.p2p.wowlet.fragment.backupwallat.completebackupwallet.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentCompleteBackupWalletBinding
import com.p2p.wowlet.fragment.backupwallat.completebackupwallet.viewmodel.CompleteBackupWalletViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CompleteBackupWalletFragment : FragmentBaseMVVM<CompleteBackupWalletViewModel,FragmentCompleteBackupWalletBinding>() {

    override val viewModel: CompleteBackupWalletViewModel by viewModel()
    override val binding: FragmentCompleteBackupWalletBinding by dataBinding(R.layout.fragment_complete_backup_wallet)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel=this@CompleteBackupWalletFragment.viewModel
        }
    }
    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand ->navigateFragment(command.destinationId)
            is Command.OpenMainActivityViewCommand -> {
                activity?.let{
                    val intent = Intent (it, MainActivity::class.java)
                    it.startActivity(intent)
                    it.finish()
                }

            }
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }
}