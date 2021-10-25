package org.p2p.wallet.restore.ui.keys

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSecretKeyBinding
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsFragment
import org.p2p.wallet.restore.ui.keys.adapter.SecretPhraseAdapter
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.hideKeyboard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class SecretKeyFragment :
    BaseMvpFragment<SecretKeyContract.View, SecretKeyContract.Presenter>(R.layout.fragment_secret_key),
    SecretKeyContract.View {

    companion object {
        fun create() = SecretKeyFragment()
    }

    override val presenter: SecretKeyContract.Presenter by inject()
    private val binding: FragmentSecretKeyBinding by viewBinding()

    private val phraseAdapter: SecretPhraseAdapter by lazy {
        SecretPhraseAdapter { presenter.setNewKeys(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
                it.hideKeyboard()
            }
            toolbar.inflateMenu(R.menu.menu_secret_key)
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.actionDone) {
                    presenter.verifySeedPhrase()
                    return@setOnMenuItemClickListener true
                }

                return@setOnMenuItemClickListener false
            }

            with(keysRecyclerView) {
                layoutManager = FlexboxLayoutManager(requireContext()).also {
                    it.flexDirection = FlexDirection.ROW
                    it.justifyContent = JustifyContent.FLEX_START
                }
                attachAdapter(phraseAdapter)
            }
            phraseTextView.setOnClickListener {
                phraseTextView.isVisible = false
                keysRecyclerView.isVisible = true
                errorTextView.text = ""
                phraseAdapter.addSecretKey(SecretKey())
            }
            resetButton.setOnClickListener { resetPhrase() }

            showActionButtons(false)
        }
    }

    override fun showSuccess(secretKeys: List<SecretKey>) {
        replaceFragment(DerivableAccountsFragment.create(secretKeys))
    }

    override fun showActionButtons(isVisible: Boolean) {
        val item = binding.toolbar.menu.findItem(R.id.actionDone)
        item.isVisible = isVisible

        binding.resetButton.isVisible = isVisible
    }

    override fun showError(messageRes: Int) {
        binding.errorTextView.setText(messageRes)
    }

    private fun resetPhrase() {
        phraseAdapter.clear()
        presenter.setNewKeys(emptyList())
    }
}