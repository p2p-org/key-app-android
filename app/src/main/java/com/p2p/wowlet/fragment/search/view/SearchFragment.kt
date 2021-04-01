package com.p2p.wowlet.fragment.search.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentSearchBinding
import com.p2p.wowlet.fragment.regwallet.view.RegWalletFragment
import com.p2p.wowlet.fragment.search.viewmodel.SearchViewModel
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replace
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : FragmentBaseMVVM<SearchViewModel, FragmentSearchBinding>() {

    override val viewModel: SearchViewModel by viewModel()
    override val binding: FragmentSearchBinding by dataBinding(R.layout.fragment_search)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@SearchFragment.viewModel
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> popBackStack()
            is Command.NavigateRegWalletViewCommand -> replace(RegWalletFragment())
        }
    }

}