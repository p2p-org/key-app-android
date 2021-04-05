package com.p2p.wowlet.fragment.reglogin.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command.NavigateRecoveryWalletViewCommand
import com.p2p.wowlet.appbase.viewcommand.Command.NavigateTermAndConditionViewCommand
import com.p2p.wowlet.appbase.viewcommand.Command.NavigateUpViewCommand
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentRegLoginBinding
import com.p2p.wowlet.fragment.backupwallat.recoverywallat.view.RecoveryWalletFragment
import com.p2p.wowlet.fragment.reglogin.viewmodel.RegLoginViewModel
import com.p2p.wowlet.fragment.termandcondition.view.TermsAndConditionFragment
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replace
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegLoginFragment : FragmentBaseMVVM<RegLoginViewModel, FragmentRegLoginBinding>() {

    override val viewModel: RegLoginViewModel by viewModel()
    override val binding: FragmentRegLoginBinding by dataBinding(R.layout.fragment_reg_login)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView() {
        with(binding) {
            btCreate.setOnClickListener {
                replace(TermsAndConditionFragment())
            }
            btAlready.setOnClickListener {
                replace(RecoveryWalletFragment())
            }
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateUpViewCommand -> popBackStack()
            is NavigateTermAndConditionViewCommand -> replace(TermsAndConditionFragment())
            is NavigateRecoveryWalletViewCommand -> replace(RecoveryWalletFragment())
        }
    }
}