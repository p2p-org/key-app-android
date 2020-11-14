package com.p2p.wowlet.fragment.contacts.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentContactsBinding
import com.p2p.wowlet.fragment.contacts.viewmodel.ContactsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactsFragment : FragmentBaseMVVM<ContactsViewModel,FragmentContactsBinding>() {
    override val viewModel: ContactsViewModel by viewModel()
    override val binding: FragmentContactsBinding by dataBinding(R.layout.fragment_contacts)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@ContactsFragment.viewModel
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateRegWalletViewCommand -> navigateFragment(command.destinationId)
        }
    }

    override fun navigateUp() {
        viewModel.finishApp()
    }

}