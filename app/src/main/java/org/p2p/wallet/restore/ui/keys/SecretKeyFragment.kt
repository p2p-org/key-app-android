package org.p2p.wallet.restore.ui.keys

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.koin.android.ext.android.inject
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

class SecretKeyFragment :
    BaseMvpFragment<SecretKeyContract.View, SecretKeyContract.Presenter>(R.layout.fragment_secret_key),
    SecretKeyContract.View {

    companion object {
        fun create() = SecretKeyFragment()
    }

    override val presenter: SecretKeyContract.Presenter by inject()
    private val binding: FragmentSecretKeyBinding by viewBinding()

    private val phraseAdapter: SecretPhraseAdapter by lazy {
        SecretPhraseAdapter {
            presenter.setNewKeys(it)
            clearError()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
                it.hideKeyboard()
            }

            restoreButton.setOnClickListener {
                presenter.verifySeedPhrase()
            }

            keysRecyclerView.layoutManager = FlexboxLayoutManager(requireContext()).also {
                it.flexDirection = FlexDirection.ROW
                it.justifyContent = JustifyContent.FLEX_START
            }
            keysRecyclerView.attachAdapter(phraseAdapter)

            phraseTextView.setOnClickListener {
                phraseTextView.isVisible = false
                keysRecyclerView.isVisible = true
                phraseAdapter.addSecretKey(SecretKey())
            }
        }

        val itemsCount = phraseAdapter.itemCount
        setButtonEnabled(itemsCount != 0)
    }

    override fun showSuccess(secretKeys: List<SecretKey>) {
        replaceFragment(DerivableAccountsFragment.create(secretKeys))
    }

    override fun setButtonEnabled(isEnabled: Boolean) {
        binding.restoreButton.isEnabled = isEnabled
    }

    override fun showError(messageRes: Int) {
        binding.errorTextView.setText(messageRes)
        binding.messageTextView.isVisible = false
    }

    private fun clearError() {
        binding.errorTextView.text = ""
        binding.messageTextView.isVisible = true
    }
}