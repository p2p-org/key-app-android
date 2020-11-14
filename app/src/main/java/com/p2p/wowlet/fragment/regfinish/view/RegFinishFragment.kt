package com.p2p.wowlet.fragment.regfinish.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentRegFinishBinding
import com.p2p.wowlet.fragment.regfinish.viewmodel.RegFinishViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class RegFinishFragment : FragmentBaseMVVM<RegFinishViewModel, FragmentRegFinishBinding>() {

    override val viewModel: RegFinishViewModel by viewModel()
    override val binding: FragmentRegFinishBinding by dataBinding(R.layout.fragment_reg_finish)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@RegFinishFragment.viewModel
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
           // is Command.NavigateUpViewCommand -> navigateFragment(command.destinationId)
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
       // viewModel.navigateUp()
    }

}