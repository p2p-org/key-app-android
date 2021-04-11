package com.p2p.wowlet.backupwallat.secretkeys.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.auth.ui.pincode.view.PinCodeFragment
import com.p2p.wowlet.backupwallat.secretkeys.adapter.SecretPhraseAdapter
import com.p2p.wowlet.backupwallat.secretkeys.utils.hideSoftKeyboard
import com.p2p.wowlet.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentSecretKeyBinding
import com.p2p.wowlet.entities.enums.PinCodeFragmentType
import com.p2p.wowlet.entities.local.Keyword
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SecretKeyFragment : BaseFragment(R.layout.fragment_secret_key) {
    private val viewModel: SecretKeyViewModel by viewModel()
    private val binding: FragmentSecretKeyBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backImageView.setOnClickListener {
            popBackStack()
            requireContext().hideSoftKeyboard(this@SecretKeyFragment)
        }
        binding.rvSecretPhrase.adapter = SecretPhraseAdapter(requireContext(), viewModel)
        binding.phraseET.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.rvSecretPhrase.apply {
                    (adapter as SecretPhraseAdapter).addItem(Keyword(""))
                    v.visibility = View.GONE
                    visibility = View.VISIBLE
                    binding.txtErrorMessage.text = ""
                }
            }
        }

        binding.resetButton.setOnClickListener { viewModel.resetPhrase() }

        binding.phraseET.requestFocus()

        observeData()
    }

    private fun observeData() {
        viewModel.isCurrentCombination.observe(viewLifecycleOwner) {
            replaceFragment(PinCodeFragment.create(false, false, PinCodeFragmentType.CREATE))
        }
        viewModel.invadedPhrase.observe(viewLifecycleOwner) { errorMessage ->
            binding.txtErrorMessage.text = errorMessage
        }
        viewModel.shouldResetThePhrase.observe(viewLifecycleOwner) {
            binding.apply {
                (rvSecretPhrase.adapter as SecretPhraseAdapter?)?.clear()
                rvSecretPhrase.visibility = View.GONE
                phraseET.visibility = View.VISIBLE
                requireContext().hideSoftKeyboard(this@SecretKeyFragment)
            }
        }
    }
}