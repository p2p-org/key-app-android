package com.p2p.wowlet.fragment.createwallet.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentCreateWalletBinding
import com.p2p.wowlet.fragment.createwallet.adapter.CreateWalletAdapter
import com.p2p.wowlet.fragment.createwallet.viewmodel.CreateWalletViewModel
import com.p2p.wowlet.fragment.regwallet.view.RegWalletFragment
import com.p2p.wowlet.utils.copyClipboard
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replace
import kotlinx.android.synthetic.main.fragment_create_wallet.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class CreateWalletFragment :
    FragmentBaseMVVM<CreateWalletViewModel, FragmentCreateWalletBinding>() {
    override val viewModel: CreateWalletViewModel by viewModel()
    override val binding: FragmentCreateWalletBinding by dataBinding(R.layout.fragment_create_wallet)

    private var phrase: String? = null
    private lateinit var createWalletAdapter: CreateWalletAdapter
    private var phraseList = listOf<String>()

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
            if (phraseList.isNotEmpty()) {
                val phraseStr = phraseList.joinToString(separator = " ")
                it.context.copyClipboard(phraseStr)
            }
        }
    }

    override fun initView() {
        createWalletAdapter = CreateWalletAdapter(mutableListOf())
        vRVPhrase.adapter = createWalletAdapter
    }

    override fun observes() {
        observe(viewModel.getPhraseData) {
            phraseList = it
            createWalletAdapter.setData(it)
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> popBackStack()
            is Command.NavigateRegWalletViewCommand -> replace(RegWalletFragment())
        }
    }

    companion object {
        const val PHRASE = "phrase"
    }

}