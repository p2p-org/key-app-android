package com.p2p.wowlet.fragment.fingetprint.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command.NavigateNotificationViewCommand
import com.p2p.wowlet.appbase.viewcommand.Command.NavigateUpViewCommand
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentFingerprintBinding
import com.p2p.wowlet.fragment.fingetprint.viewmodel.FingerPrintViewModel
import com.p2p.wowlet.utils.openFingerprintDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class FingerPrintFragment : FragmentBaseMVVM<FingerPrintViewModel, FragmentFingerprintBinding>() {

    override val viewModel: FingerPrintViewModel by viewModel()
    override val binding: FragmentFingerprintBinding by dataBinding(R.layout.fragment_fingerprint)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@FingerPrintFragment.viewModel
        }
        binding.btUseFaceID.setOnClickListener {
            activity?.openFingerprintDialog() {
                viewModel.enableFingerprint()
            }
        }
    }

    override fun observes() {
        observe(viewModel.isSkipFingerPrint) {
            viewModel.doThisLater(it)
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateUpViewCommand -> {
                navigateFragment(command.destinationId)
            }
            is NavigateNotificationViewCommand -> {
                navigateFragment(command.destinationId)
            }
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }
}