package com.p2p.wowlet.fragment.createwallet.view

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentCreateWalletBinding
import com.p2p.wowlet.fragment.createwallet.adapter.CreateWalletAdapter
import com.p2p.wowlet.fragment.createwallet.viewmodel.CreateWalletViewModel
import com.p2p.wowlet.fragment.regwallet.view.RegWalletFragment
import com.p2p.wowlet.utils.copyClipboard
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateWalletFragment : BaseFragment(R.layout.fragment_create_wallet) {

    companion object {
        fun create() = CreateWalletFragment()
    }

    private val viewModel: CreateWalletViewModel by viewModel()
    private val binding: FragmentCreateWalletBinding by viewBinding()

    private val walletAdapter: CreateWalletAdapter by lazy {
        CreateWalletAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            vIcBack.setOnClickListener { popBackStack() }

            vIcRefresh.setOnClickListener {
                viewModel.generatePhrases()
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                btContinue.isEnabled = isChecked
            }
            btContinue.setOnClickListener { replaceFragment(RegWalletFragment()) }

            ibtCopyClipboard.setOnClickListener {
                val phrases = viewModel.phrasesLiveData.value ?: emptyList()

                if (phrases.isNotEmpty()) {
                    val phraseStr = phrases.joinToString(separator = " ")
                    it.context.copyClipboard(phraseStr)
                }
            }

            with(vRVPhrase) {
                adapter = walletAdapter
                layoutManager = GridLayoutManager(requireContext(), 3)
            }
        }

        viewModel.phrasesLiveData.observe(viewLifecycleOwner) {
            walletAdapter.setItems(it)
        }

        viewModel.generatePhrases()
    }
}