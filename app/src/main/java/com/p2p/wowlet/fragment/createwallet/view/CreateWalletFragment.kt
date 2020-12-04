package com.p2p.wowlet.fragment.createwallet.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentCreateWalletBinding
import com.p2p.wowlet.fragment.createwallet.viewmodel.CreateWalletViewModel
import com.p2p.wowlet.utils.copyClipboard
import kotlinx.android.synthetic.main.fragment_create_wallet.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class CreateWalletFragment :
    FragmentBaseMVVM<CreateWalletViewModel, FragmentCreateWalletBinding>() {
    override val viewModel: CreateWalletViewModel by viewModel()
    override val binding: FragmentCreateWalletBinding by dataBinding(R.layout.fragment_create_wallet)

    private var phrase: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            phrase = it.getString(PHRASE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@CreateWalletFragment.viewModel
        }
        ibtCopyClipboard.setOnClickListener {
            it.context.copyClipboard(vPhrase.text.toString())

        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateRegWalletViewCommand -> navigateFragment(command.destinationId)
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }

    companion object {
        const val PHRASE = "phrase"
    }

}