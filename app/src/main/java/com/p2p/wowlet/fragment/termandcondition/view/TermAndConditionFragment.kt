package com.p2p.wowlet.fragment.termandcondition.view

import android.os.Bundle

import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentTermAndConditionBinding
import com.p2p.wowlet.fragment.termandcondition.viewmodel.TermAndConditionViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TermAndConditionFragment() : FragmentBaseMVVM<TermAndConditionViewModel, FragmentTermAndConditionBinding>() {
    override val viewModel: TermAndConditionViewModel by viewModel()
    override val binding: FragmentTermAndConditionBinding by dataBinding(R.layout.fragment_term_and_condition)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@TermAndConditionFragment.viewModel
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateCreateWalletViewCommand -> navigateFragment(command.destinationId)
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }



}