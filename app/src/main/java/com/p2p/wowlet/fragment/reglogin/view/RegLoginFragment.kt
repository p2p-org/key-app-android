package com.p2p.wowlet.fragment.reglogin.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentRegLoginBinding
import com.p2p.wowlet.fragment.reglogin.viewmodel.RegLoginViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegLoginFragment : FragmentBaseMVVM<RegLoginViewModel, FragmentRegLoginBinding>() {

    override val viewModel: RegLoginViewModel by viewModel()
    override val binding: FragmentRegLoginBinding by dataBinding(R.layout.fragment_reg_login)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@RegLoginFragment.viewModel
        }
    }

    override fun initView() {
        with(binding) {

        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is NavigateTermAndConditionViewCommand -> navigateFragment(command.destinationId)
            is NavigateRecoveryWalletViewCommand -> navigateFragment(command.destinationId)
        }
    }

    override fun navigateUp() {
        viewModel.finishApp()
    }

}