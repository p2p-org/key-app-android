package com.p2p.wowlet.fragment.pincode.view

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentPinCodeBinding
import com.p2p.wowlet.fragment.pincode.viewmodel.PinCodeViewModel
import com.raycoarana.codeinputview.CodeInputView
import org.koin.androidx.viewmodel.ext.android.viewModel


class PinCodeFragment : FragmentBaseMVVM<PinCodeViewModel, FragmentPinCodeBinding>() {

    override val viewModel: PinCodeViewModel by viewModel()
    override val binding: FragmentPinCodeBinding by dataBinding(R.layout.fragment_pin_code)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@PinCodeFragment.viewModel
        }
        with(binding) {
            inputCode.apply {
                addOnCompleteListener { value ->
                    this@PinCodeFragment.viewModel.initCode(value)
                }
            }
        }

        observes()
    }

    private fun observes() {
        observe(viewModel.checkPinCode) {
            if(it){
                viewModel.goToFaceIdFragment()
            }else{
                with(binding){
                    pinCodeError(inputCode)
                }
            }
        }
    }


    private fun pinCodeError(inputCode: CodeInputView) {
        inputCode.setEditable(true)
        inputCode.error = getString(R.string.error_code)
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateFaceIdViewCommand -> {
                navigateFragment(command.destinationId)
            }
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }
}