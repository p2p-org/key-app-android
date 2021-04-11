package com.p2p.wowlet.backupwallat.completebackupwallet.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.backupwallat.completebackupwallet.viewmodel.CompleteBackupWalletViewModel
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentCompleteBackupWalletBinding
import com.p2p.wowlet.home.HomeFragment
import com.p2p.wowlet.utils.popAndReplaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CompleteBackupWalletFragment : BaseFragment(R.layout.fragment_complete_backup_wallet) {

    private val viewModel: CompleteBackupWalletViewModel by viewModel()

    private val binding: FragmentCompleteBackupWalletBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btBackupManual.setOnClickListener {
                viewModel.finishRegistration()
                popAndReplaceFragment(HomeFragment.create(), inclusive = true)
            }
        }
    }
}