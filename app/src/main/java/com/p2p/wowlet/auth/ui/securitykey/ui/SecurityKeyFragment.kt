package com.p2p.wowlet.auth.ui.securitykey.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.p2p.wowlet.R
import com.p2p.wowlet.auth.ui.RegWalletFragment
import com.p2p.wowlet.common.mvp.BaseMvpFragment
import com.p2p.wowlet.databinding.FragmentSecurityKeyBinding
import com.p2p.wowlet.utils.copyClipboard
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class SecurityKeyFragment :
    BaseMvpFragment<SecurityKeyContract.View, SecurityKeyContract.Presenter>(R.layout.fragment_security_key),
    SecurityKeyContract.View {

    companion object {
        fun create() = SecurityKeyFragment()
    }

    override val presenter: SecurityKeyContract.Presenter by inject()

    private val binding: FragmentSecurityKeyBinding by viewBinding()

    private val phrasesAdapter: PhrasesAdapter by lazy {
        PhrasesAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            vIcBack.setOnClickListener { popBackStack() }

            vIcRefresh.setOnClickListener {
                presenter.loadPhrases()
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                btContinue.isEnabled = isChecked
            }
            btContinue.setOnClickListener { replaceFragment(RegWalletFragment()) }

            ibtCopyClipboard.setOnClickListener {
                presenter.copyPhrases()
            }

            with(vRVPhrase) {
                adapter = phrasesAdapter
                layoutManager = GridLayoutManager(requireContext(), 3)
            }
        }

        presenter.loadPhrases()
    }

    override fun showPhrases(phrases: List<String>) {
        phrasesAdapter.setItems(phrases)
    }

    override fun copyToClipboard(phrases: List<String>) {
        if (phrases.isNotEmpty()) {
            val data = phrases.joinToString(separator = " ")
            requireContext().copyClipboard(data)
        }
    }
}