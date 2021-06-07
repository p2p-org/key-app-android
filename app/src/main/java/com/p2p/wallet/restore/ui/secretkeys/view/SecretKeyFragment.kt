package com.p2p.wallet.restore.ui.secretkeys.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.pin.create.CreatePinFragment
import com.p2p.wallet.auth.ui.pin.create.PinLaunchMode
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentSecretKeyBinding
import com.p2p.wallet.main.model.Keyword
import com.p2p.wallet.restore.ui.secretkeys.adapter.SecretPhraseAdapter
import com.p2p.wallet.restore.ui.secretkeys.utils.hideSoftKeyboard
import com.p2p.wallet.restore.ui.secretkeys.viewmodel.SecretKeyViewModel
import com.p2p.wallet.utils.attachAdapter
import com.p2p.wallet.utils.popAndReplaceFragment
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@Deprecated("Should be refactored")
class SecretKeyFragment : BaseFragment(R.layout.fragment_secret_key) {

    companion object {
        private const val KEYS_MAX_COUNT = 12
        fun create() = SecretKeyFragment()
    }

    private val viewModel: SecretKeyViewModel by viewModel()
    private val binding: FragmentSecretKeyBinding by viewBinding()

    private val phraseAdapter: SecretPhraseAdapter by lazy {
        SecretPhraseAdapter(requireContext(), viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
                requireContext().hideSoftKeyboard(this@SecretKeyFragment)
            }

            keysRecyclerView.attachAdapter(phraseAdapter)

            phraseEditText.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    keysRecyclerView.apply {
                        phraseAdapter.addItem(Keyword(""))
                        v.visibility = View.GONE
                        visibility = View.VISIBLE
                        errorTextView.text = ""
                    }
                }
            }

            resetButton.setOnClickListener { viewModel.resetPhrase() }

            phraseEditText.requestFocus()

            observeData()
        }
    }

    private fun observeData() {
        with(viewModel) {
            isCurrentCombination.observe(viewLifecycleOwner) {
                popAndReplaceFragment(CreatePinFragment.create(PinLaunchMode.RECOVER), inclusive = true)
            }
            invadedPhrase.observe(viewLifecycleOwner) { errorMessage ->
                binding.errorTextView.text = errorMessage
            }
            phrase.observe(viewLifecycleOwner) {
                val keysCount = it.split(" ").size
                binding.resetButton.isVisible = keysCount == KEYS_MAX_COUNT
            }
            shouldResetThePhrase.observe(viewLifecycleOwner) {
                binding.apply {
                    phraseAdapter.clear()
                    keysRecyclerView.visibility = View.GONE
                    phraseEditText.visibility = View.VISIBLE
                    requireContext().hideSoftKeyboard(this@SecretKeyFragment)
                }
            }
        }
    }
}