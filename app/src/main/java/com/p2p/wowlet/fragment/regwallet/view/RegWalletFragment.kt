package com.p2p.wowlet.fragment.regwallet.view

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentRegWalletBinding
import com.p2p.wowlet.fragment.regwallet.viewmodel.RegWalletViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class RegWalletFragment : FragmentBaseMVVM<RegWalletViewModel, FragmentRegWalletBinding>() {

    private lateinit var yourCountDownTimer: CountDownTimer
    override val viewModel: RegWalletViewModel by viewModel()
    override val binding: FragmentRegWalletBinding by dataBinding(R.layout.fragment_reg_wallet)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@RegWalletFragment.viewModel
        }
        yourCountDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                viewModel.goToPinCodeFragment()
            }
        }.start()

    }
    override fun processViewCommand(command: ViewCommand) {
        when(command){
            is Command.NavigatePinCodeViewCommand -> navigateFragment(command.destinationId,command.bundle)
            is Command.NavigateUpViewCommand -> navigateFragment(command.destinationId)
        }
    }
    override fun navigateUp() {
        yourCountDownTimer.cancel()
        viewModel.navigateUp()
    }
}